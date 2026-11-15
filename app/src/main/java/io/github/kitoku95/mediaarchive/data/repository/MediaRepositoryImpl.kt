package io.github.kitoku95.mediaarchive.data.repository

import android.net.Uri
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.data.mapper.toDomain
import io.github.kitoku95.mediaarchive.data.storage.SafMediaScanner
import io.github.kitoku95.mediaarchive.data.storage.ScannedMediaFile
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaItemDao: MediaItemDao,
    private val safMediaScanner: SafMediaScanner,
    private val thumbnailStorageManager: ThumbnailStorageManager
) : MediaRepository {

    override fun observeMediaByCategoryAndType(
        categoryId: Long,
        mediaType: MediaType
    ): Flow<List<MediaItem>> {
        return mediaItemDao.observeMediaByCategoryAndType(
            categoryId = categoryId,
            mediaType = mediaType.name
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun importSelectedMedia(
        categoryId: Long,
        uriStrings: List<String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val scannedFiles = uriStrings.mapNotNull { uriString ->
                if (mediaItemDao.existsInCategory(categoryId, uriString)) {
                    null
                } else {
                    safMediaScanner.inspectSingleFile(Uri.parse(uriString))
                }
            }
            persistScannedMedia(categoryId, scannedFiles)
        }
    }

    override suspend fun importFolderMedia(
        categoryId: Long,
        treeUriString: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val scannedFiles = safMediaScanner.scanFolder(Uri.parse(treeUriString))
                .filter { scanned ->
                    !mediaItemDao.existsInCategory(categoryId, scanned.contentUri.toString())
                }
            persistScannedMedia(categoryId, scannedFiles)
        }
    }

    override suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = mediaItemDao.getMediaItemById(mediaItemId)
                ?: throw IllegalArgumentException("Media item not found.")
            mediaItemDao.deleteMediaItemById(mediaItemId)
            thumbnailStorageManager.deleteThumbnail(entity.thumbnailPath)
        }
    }

    override suspend fun isMediaAccessible(contentUri: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            safMediaScanner.isUriAccessible(Uri.parse(contentUri))
        }.getOrDefault(false)
    }

    private suspend fun persistScannedMedia(
        categoryId: Long,
        scannedFiles: List<ScannedMediaFile>
    ): Int {
        if (scannedFiles.isEmpty()) return 0

        val entitiesToInsert = mutableListOf<MediaItemEntity>()
        for (scanned in scannedFiles) {
            val thumbnailPath = thumbnailStorageManager.generateAndSaveThumbnail(
                contentUri = scanned.contentUri,
                mediaType = scanned.mediaType
            ) ?: continue

            entitiesToInsert.add(
                MediaItemEntity(
                    categoryId = categoryId,
                    mediaType = scanned.mediaType.name,
                    contentUri = scanned.contentUri.toString(),
                    thumbnailPath = thumbnailPath,
                    displayName = scanned.displayName,
                    mimeType = scanned.mimeType,
                    durationMs = scanned.durationMs,
                    sizeBytes = scanned.sizeBytes,
                    addedAt = System.currentTimeMillis()
                )
            )
        }

        if (entitiesToInsert.isEmpty()) return 0
        val insertedRowIds = mediaItemDao.insertMediaItems(entitiesToInsert)
        return insertedRowIds.count { it != -1L }
    }
}
