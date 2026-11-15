package io.github.kitoku95.mediaarchive.domain.repository

import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for observing, manually importing via SAF, and deleting media items.
 */
interface MediaRepository {

    fun observeMediaByCategoryAndType(
        categoryId: Long,
        mediaType: MediaType
    ): Flow<List<MediaItem>>

    suspend fun importSelectedMedia(
        categoryId: Long,
        uriStrings: List<String>
    ): Result<Int>

    suspend fun importFolderMedia(
        categoryId: Long,
        treeUriString: String
    ): Result<Int>

    suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit>
}
