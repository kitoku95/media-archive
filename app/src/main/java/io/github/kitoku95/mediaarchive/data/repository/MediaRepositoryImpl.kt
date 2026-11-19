package io.github.kitoku95.mediaarchive.data.repository

import android.net.Uri
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.data.mapper.toDomain
import io.github.kitoku95.mediaarchive.data.storage.SafCanonicalIdResolver
import io.github.kitoku95.mediaarchive.data.storage.SafMediaScanner
import io.github.kitoku95.mediaarchive.data.storage.ScannedMediaFile
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
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
    private val canonicalIdResolver: SafCanonicalIdResolver,
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
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val seenCanonicalIds = HashSet<String>()
            var skippedDuplicates = 0
            val scannedFiles = mutableListOf<ScannedMediaFile>()

            for (uriString in uriStrings) {
                val uri = Uri.parse(uriString)
                val canonicalId = canonicalIdResolver.resolve(uri)
                if (!seenCanonicalIds.add(canonicalId) ||
                    mediaItemDao.existsByCanonicalId(categoryId, canonicalId)
                ) {
                    skippedDuplicates++
                    continue
                }
                safMediaScanner.inspectSingleFile(uri)?.let { scanned ->
                    scannedFiles.add(scanned)
                }
            }

            persistScannedMedia(
                categoryId = categoryId,
                scannedFiles = scannedFiles,
                initialSkippedDuplicates = skippedDuplicates
            )
        }
    }

    override suspend fun importFolderMedia(
        categoryId: Long,
        treeUriString: String
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val allScannedFiles = safMediaScanner.scanFolder(Uri.parse(treeUriString))
            val seenCanonicalIds = HashSet<String>()
            var skippedDuplicates = 0
            val filesToPersist = mutableListOf<ScannedMediaFile>()

            for (file in allScannedFiles) {
                if (!seenCanonicalIds.add(file.canonicalId) ||
                    mediaItemDao.existsByCanonicalId(categoryId, file.canonicalId)
                ) {
                    skippedDuplicates++
                } else {
                    filesToPersist.add(file)
                }
            }

            persistScannedMedia(
                categoryId = categoryId,
                scannedFiles = filesToPersist,
                initialSkippedDuplicates = skippedDuplicates
            )
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
        scannedFiles: List<ScannedMediaFile>,
        initialSkippedDuplicates: Int
    ): ImportResult {
        if (scannedFiles.isEmpty()) {
            return ImportResult(
                importedCount = 0,
                skippedDuplicateCount = initialSkippedDuplicates
            )
        }

        val entitiesToInsert = mutableListOf<MediaItemEntity>()
        for (scanned in scannedFiles) {
            val thumbnailPath = if (scanned.mediaType == MediaType.DOCUMENT) {
                ""
            } else {
                thumbnailStorageManager.generateAndSaveThumbnail(
                    contentUri = scanned.contentUri,
                    mediaType = scanned.mediaType
                ) ?: continue
            }

            entitiesToInsert.add(
                MediaItemEntity(
                    categoryId = categoryId,
                    mediaType = scanned.mediaType.name,
                    contentUri = scanned.contentUri.toString(),
                    canonicalId = scanned.canonicalId,
                    thumbnailPath = thumbnailPath,
                    displayName = scanned.displayName,
                    mimeType = scanned.mimeType,
                    durationMs = scanned.durationMs,
                    sizeBytes = scanned.sizeBytes,
                    addedAt = System.currentTimeMillis()
                )
            )
        }

        if (entitiesToInsert.isEmpty()) {
            return ImportResult(
                importedCount = 0,
                skippedDuplicateCount = initialSkippedDuplicates
            )
        }

        val insertedRowIds = mediaItemDao.insertMediaItems(entitiesToInsert)
        var importedCount = 0
        var conflictSkipped = 0
        insertedRowIds.forEachIndexed { index, rowId ->
            if (rowId != -1L) {
                importedCount++
            } else {
                conflictSkipped++
                val orphanThumbnail = entitiesToInsert[index].thumbnailPath
                if (orphanThumbnail.isNotBlank()) {
                    thumbnailStorageManager.deleteThumbnail(orphanThumbnail)
                }
            }
        }

        return ImportResult(
            importedCount = importedCount,
            skippedDuplicateCount = initialSkippedDuplicates + conflictSkipped
        )
    }
}
