package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Traverses a user-selected SAF folder tree URI and imports all discovered media into the category.
 */
class ImportFolderMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(categoryId: Long, treeUriString: String): Result<Int> {
        if (treeUriString.isBlank()) {
            return Result.success(0)
        }
        return mediaRepository.importFolderMedia(
            categoryId = categoryId,
            treeUriString = treeUriString
        )
    }
}
