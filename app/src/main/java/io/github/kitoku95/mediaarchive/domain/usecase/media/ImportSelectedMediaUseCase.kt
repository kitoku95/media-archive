package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import javax.inject.Inject

/**
 * Imports user-selected individual media or document URIs matching the category's [CategoryType].
 */
class ImportSelectedMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        categoryType: CategoryType,
        uriStrings: List<String>
    ): Result<ImportResult> {
        if (uriStrings.isEmpty()) {
            return Result.success(ImportResult(importedCount = 0, skippedDuplicateCount = 0))
        }
        return mediaRepository.importSelectedMedia(
            categoryId = categoryId,
            categoryType = categoryType,
            uriStrings = uriStrings
        )
    }
}
