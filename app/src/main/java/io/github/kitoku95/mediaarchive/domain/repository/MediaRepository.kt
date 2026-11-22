package io.github.kitoku95.mediaarchive.domain.repository

import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for observing, manually importing via SAF, and deleting media and document items.
 */
interface MediaRepository {

    fun observeMediaByCategoryAndType(
        categoryId: Long,
        mediaType: MediaType
    ): Flow<List<MediaItem>>

    suspend fun importSelectedMedia(
        categoryId: Long,
        categoryType: CategoryType,
        uriStrings: List<String>
    ): Result<ImportResult>

    suspend fun importFolderMedia(
        categoryId: Long,
        categoryType: CategoryType,
        treeUriString: String
    ): Result<ImportResult>

    suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit>

    suspend fun isMediaAccessible(contentUri: String): Boolean
}
