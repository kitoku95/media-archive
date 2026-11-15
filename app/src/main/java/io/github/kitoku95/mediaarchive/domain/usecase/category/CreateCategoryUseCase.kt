package io.github.kitoku95.mediaarchive.domain.usecase.category

import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Creates a new `public` or `private` Category.
 *
 * Per HR-03 / ADR-003, any non-empty password (`password.isNotEmpty()`) is valid
 * for `private` Categories without minimum length or complexity restrictions.
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        accessMode: AccessMode,
        password: String?
    ): Result<Category> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Category name must not be blank."))
        }
        if (accessMode == AccessMode.PRIVATE && password.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Password is required for private categories."))
        }
        return categoryRepository.createCategory(
            name = trimmedName,
            accessMode = accessMode,
            password = if (accessMode == AccessMode.PUBLIC) null else password
        )
    }
}
