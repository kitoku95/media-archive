package io.github.kitoku95.mediaarchive.domain.usecase.category

import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Deletes a custom Category (excluding default `Main`) and cleans up its cached thumbnails.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> {
        return categoryRepository.deleteCategory(categoryId)
    }
}
