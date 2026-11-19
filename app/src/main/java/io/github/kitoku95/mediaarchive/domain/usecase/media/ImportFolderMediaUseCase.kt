package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Recursively traverses a user-selected SAF folder tree URI and imports all discovered files
 * directly into the category.
 */
class ImportFolderMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        treeUriString: String
    ): Result<ImportResult> {
        if (treeUriString.isBlank()) {
            return Result.success(ImportResult(importedCount = 0, skippedDuplicateCount = 0))
        }
        return mediaRepository.importFolderMedia(
            categoryId = categoryId,
            treeUriString = treeUriString
        )
    }
}
