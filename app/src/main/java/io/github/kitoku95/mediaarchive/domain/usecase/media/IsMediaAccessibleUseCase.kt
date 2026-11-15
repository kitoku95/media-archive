package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Checks whether an external media `content_uri` is still accessible via SAF.
 */
class IsMediaAccessibleUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(contentUri: String): Boolean {
        if (contentUri.isBlank()) return false
        return mediaRepository.isMediaAccessible(contentUri)
    }
}
