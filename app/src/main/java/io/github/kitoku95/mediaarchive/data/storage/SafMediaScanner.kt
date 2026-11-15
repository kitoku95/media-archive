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
    @ApplicationContext private val context: Context
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
        val mimeType = context.contentResolver.getType(uri) ?: return@withContext null
        val mediaType = resolveMediaType(mimeType) ?: return@withContext null

        val (displayName, sizeBytes) = queryDisplayNameAndSize(uri)
        val durationMs = if (mediaType == MediaType.VIDEO) {
            extractVideoDurationMs(uri)
        } else {
            null
        }

        ScannedMediaFile(
            contentUri = uri,
            mediaType = mediaType,
            displayName = displayName,
            mimeType = mimeType,
            durationMs = durationMs,
            sizeBytes = sizeBytes
        )
    }

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
                    stack.addLast(child)
                } else if (child.isFile) {
                    val mimeType = child.type ?: context.contentResolver.getType(child.uri) ?: continue
                    val mediaType = resolveMediaType(mimeType) ?: continue
                    val displayName = child.name?.takeIf { it.isNotBlank() }
                        ?: queryDisplayNameAndSize(child.uri).first
                    val sizeBytes = child.length().takeIf { it > 0L }
                        ?: queryDisplayNameAndSize(child.uri).second
                    val durationMs = if (mediaType == MediaType.VIDEO) {
                        extractVideoDurationMs(child.uri)
                    } else {
                        null
                    }
                    discovered.add(
                        ScannedMediaFile(
                            contentUri = child.uri,
                            mediaType = mediaType,
                            displayName = displayName,
                            mimeType = mimeType,
                            durationMs = durationMs,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }
        }
        discovered
    }

    private fun resolveMediaType(mimeType: String): MediaType? = when {
        mimeType.startsWith("image/", ignoreCase = true) -> MediaType.IMAGE
        mimeType.startsWith("video/", ignoreCase = true) -> MediaType.VIDEO
        else -> null
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
    }
}
