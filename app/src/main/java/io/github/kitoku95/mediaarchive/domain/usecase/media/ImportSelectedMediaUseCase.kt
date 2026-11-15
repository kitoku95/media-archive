package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Imports user-selected individual media URIs into the specified category.
 */
class ImportSelectedMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(categoryId: Long, uriStrings: List<String>): Result<Int> {
        if (uriStrings.isEmpty()) {
            return Result.success(0)
        }
        return mediaRepository.importSelectedMedia(
            categoryId = categoryId,
            uriStrings = uriStrings
        )
    }
}
