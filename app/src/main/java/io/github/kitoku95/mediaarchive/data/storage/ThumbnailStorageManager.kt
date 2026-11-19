package io.github.kitoku95.mediaarchive.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates and stores 512x512 WEBP thumbnails inside app-private `filesDir/thumbnails/`
 * with a `.nomedia` marker file and UUID filenames.
 */
@Singleton
class ThumbnailStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val thumbnailsDir: File
        get() = File(context.filesDir, THUMBNAILS_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
            val nomedia = File(this, NOMEDIA_FILE_NAME)
            if (!nomedia.exists()) {
                runCatching { nomedia.createNewFile() }
            }
        }

    /**
     * Extracts a 512x512 thumbnail for [contentUri], writes it as `<uuid>.webp` inside
     * `filesDir/thumbnails/`, and returns the absolute file path, or `null` if extraction fails.
     */
    suspend fun generateAndSaveThumbnail(
        contentUri: Uri,
        mediaType: MediaType
    ): String? = withContext(Dispatchers.IO) {
        val rawBitmap = extractBitmap(contentUri, mediaType) ?: return@withContext null
        val squareBitmap = centerCropToSquare(rawBitmap, THUMBNAIL_SIZE_PX)
        if (squareBitmap !== rawBitmap) {
            rawBitmap.recycle()
        }

        val outputFile = File(thumbnailsDir, "${UUID.randomUUID()}.webp")
        val saved = runCatching {
            FileOutputStream(outputFile).use { out ->
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                squareBitmap.compress(format, WEBP_QUALITY, out)
            }
        }.getOrElse {
            outputFile.delete()
            false
        }

        squareBitmap.recycle()
        if (saved) outputFile.absolutePath else null
    }

    suspend fun deleteThumbnail(thumbnailPath: String) = withContext(Dispatchers.IO) {
        deleteIfInsideThumbnailsDir(thumbnailPath)
    }

    suspend fun deleteThumbnails(thumbnailPaths: List<String>) = withContext(Dispatchers.IO) {
        thumbnailPaths.forEach(::deleteIfInsideThumbnailsDir)
    }

    private fun deleteIfInsideThumbnailsDir(thumbnailPath: String) {
        if (thumbnailPath.isBlank()) return
        runCatching {
            val baseCanonical = thumbnailsDir.canonicalPath + File.separator
            val target = File(thumbnailPath)
            if (target.canonicalPath.startsWith(baseCanonical) && target.exists()) {
                target.delete()
            }
        }
    }

    private fun extractBitmap(contentUri: Uri, mediaType: MediaType): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val systemThumbnail = runCatching {
                context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX),
                    null
                )
            }.getOrNull()
            if (systemThumbnail != null) {
                return systemThumbnail
            }
        }

        return when (mediaType) {
            MediaType.IMAGE -> extractImageBitmapFallback(contentUri)
            MediaType.VIDEO -> extractVideoBitmapFallback(contentUri)
            MediaType.DOCUMENT -> null
        }
    }

    private fun extractImageBitmapFallback(contentUri: Uri): Bitmap? {
        return runCatching {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(contentUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }

            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                reqWidth = THUMBNAIL_SIZE_PX,
                reqHeight = THUMBNAIL_SIZE_PX
            )
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            context.contentResolver.openInputStream(contentUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }.getOrNull()
    }

    private fun extractVideoBitmapFallback(contentUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, contentUri)
            retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun centerCropToSquare(source: Bitmap, targetSizePx: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(
            source,
            targetSizePx,
            targetSizePx,
            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        )
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private companion object {
        const val THUMBNAILS_DIR_NAME = "thumbnails"
        const val NOMEDIA_FILE_NAME = ".nomedia"
        const val THUMBNAIL_SIZE_PX = 512
        const val WEBP_QUALITY = 80
    }
}
