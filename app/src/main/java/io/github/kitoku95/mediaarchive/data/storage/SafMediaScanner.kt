package io.github.kitoku95.mediaarchive.data.storage

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Metadata extracted from a user-selected SAF document before thumbnail generation.
 */
data class ScannedMediaFile(
    val contentUri: Uri,
    val canonicalId: String,
    val mediaType: MediaType,
    val displayName: String,
    val mimeType: String,
    val durationMs: Long?,
    val sizeBytes: Long
)

/**
 * Storage Access Framework (SAF) scanner for user-selected files and folders.
 *
 * Strictly operates only on URIs explicitly chosen by the user (no background storage scanning).
 */
@Singleton
class SafMediaScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canonicalIdResolver: SafCanonicalIdResolver
) {

    fun takePersistableReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    suspend fun inspectSingleFile(uri: Uri): ScannedMediaFile? = withContext(Dispatchers.IO) {
        takePersistableReadPermission(uri)
        val (displayName, sizeBytes) = queryDisplayNameAndSize(uri)
        if (shouldSkipFile(displayName)) return@withContext null

        val rawMimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
        val mediaType = resolveMediaType(rawMimeType, displayName) ?: return@withContext null
        val effectiveMimeType = rawMimeType.ifBlank { fallbackMimeTypeForExtension(displayName) }

        val durationMs = if (mediaType == MediaType.VIDEO) {
            extractVideoDurationMs(uri)
        } else {
            null
        }

        ScannedMediaFile(
            contentUri = uri,
            canonicalId = canonicalIdResolver.resolve(uri),
            mediaType = mediaType,
            displayName = displayName,
            mimeType = effectiveMimeType,
            durationMs = durationMs,
            sizeBytes = sizeBytes
        )
    }

    /**
     * Recursively traverses [treeUri] and returns a flat list of all supported Images, Videos,
     * and Documents, skipping hidden/system cache folders (such as `.thumbnails`) and hidden files.
     */
    suspend fun scanFolder(treeUri: Uri): List<ScannedMediaFile> = withContext(Dispatchers.IO) {
        takePersistableReadPermission(treeUri)
        val rootDir = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return@withContext emptyList()
        }

        val discovered = mutableListOf<ScannedMediaFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.addLast(rootDir)

        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            val children = runCatching { current.listFiles() }.getOrDefault(emptyArray())
            for (child in children) {
                if (child.isDirectory) {
                    if (!shouldSkipDirectory(child.name)) {
                        stack.addLast(child)
                    }
                } else if (child.isFile) {
                    val rawName = child.name
                    if (shouldSkipFile(rawName)) continue

                    val (queriedName, queriedSize) = queryDisplayNameAndSize(child.uri)
                    val displayName = rawName?.takeIf { it.isNotBlank() } ?: queriedName
                    if (shouldSkipFile(displayName)) continue

                    val rawMimeType = runCatching {
                        child.type ?: context.contentResolver.getType(child.uri)
                    }.getOrNull().orEmpty()
                    val mediaType = resolveMediaType(rawMimeType, displayName) ?: continue
                    val effectiveMimeType = rawMimeType.ifBlank { fallbackMimeTypeForExtension(displayName) }
                    val sizeBytes = child.length().takeIf { it > 0L } ?: queriedSize
                    val durationMs = if (mediaType == MediaType.VIDEO) {
                        extractVideoDurationMs(child.uri)
                    } else {
                        null
                    }
                    discovered.add(
                        ScannedMediaFile(
                            contentUri = child.uri,
                            canonicalId = canonicalIdResolver.resolve(child.uri),
                            mediaType = mediaType,
                            displayName = displayName,
                            mimeType = effectiveMimeType,
                            durationMs = durationMs,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }
        }
        discovered
    }

    suspend fun isUriAccessible(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val canOpenDescriptor = runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        }.getOrDefault(false)
        if (canOpenDescriptor) return@withContext true

        runCatching {
            DocumentFile.fromSingleUri(context, uri)?.exists() == true
        }.getOrDefault(false)
    }

    /**
     * Skips hidden/system cache folders such as `.thumbnails`, `.trash`, `.cache`, etc.
     */
    internal fun shouldSkipDirectory(directoryName: String?): Boolean {
        val trimmed = directoryName?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        return trimmed.startsWith(".") || trimmed.equals("thumbnails", ignoreCase = true)
    }

    /**
     * Skips hidden/system files such as `.nomedia` or `.trashed-*`.
     */
    internal fun shouldSkipFile(fileName: String?): Boolean {
        val trimmed = fileName?.trim().orEmpty()
        return trimmed.startsWith(".")
    }

    internal fun resolveMediaType(mimeType: String, displayName: String): MediaType? {
        val normalizedMime = mimeType.lowercase()
        when {
            normalizedMime.startsWith("image/") -> return MediaType.IMAGE
            normalizedMime.startsWith("video/") -> return MediaType.VIDEO
            normalizedMime.startsWith("text/") -> return MediaType.DOCUMENT
            normalizedMime in DOCUMENT_MIME_TYPES -> return MediaType.DOCUMENT
            normalizedMime.startsWith("application/vnd.openxmlformats-officedocument.") -> return MediaType.DOCUMENT
            normalizedMime.startsWith("application/vnd.oasis.opendocument.") -> return MediaType.DOCUMENT
        }

        val ext = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (ext in DOCUMENT_EXTENSIONS) {
            return MediaType.DOCUMENT
        }
        return null
    }

    private fun fallbackMimeTypeForExtension(displayName: String): String {
        return when (displayName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "txt", "md", "csv" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "rtf" -> "application/rtf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "odp" -> "application/vnd.oasis.opendocument.presentation"
            else -> "application/octet-stream"
        }
    }

    private fun queryDisplayNameAndSize(uri: Uri): Pair<String, Long> {
        var displayName = uri.lastPathSegment?.substringAfterLast('/') ?: DEFAULT_FILE_NAME
        var sizeBytes = 0L

        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let {
                            displayName = it
                        }
                    }
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
            }
        }
        return displayName to sizeBytes
    }

    private fun extractVideoDurationMs(uri: Uri): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private companion object {
        const val DEFAULT_FILE_NAME = "media_file"

        val DOCUMENT_MIME_TYPES = setOf(
            "application/pdf",
            "application/rtf",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint"
        )

        val DOCUMENT_EXTENSIONS = setOf(
            "txt", "md", "csv", "json", "xml", "rtf", "pdf",
            "doc", "docx", "odt",
            "xls", "xlsx", "ods",
            "ppt", "pptx", "odp"
        )
    }
}
