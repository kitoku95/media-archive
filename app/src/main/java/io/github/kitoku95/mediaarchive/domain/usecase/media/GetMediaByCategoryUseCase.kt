package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes media items of a specific [MediaType] belonging to a category.
 */
class GetMediaByCategoryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(categoryId: Long, mediaType: MediaType): Flow<List<MediaItem>> {
        return mediaRepository.observeMediaByCategoryAndType(
            categoryId = categoryId,
            mediaType = mediaType
        )
    }
}
