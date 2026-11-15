package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Deletes a media item from SQLite and removes its cached thumbnail.
 */
class DeleteMediaItemUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(mediaItemId: Long): Result<Unit> {
        return mediaRepository.deleteMediaItem(mediaItemId)
    }
}
