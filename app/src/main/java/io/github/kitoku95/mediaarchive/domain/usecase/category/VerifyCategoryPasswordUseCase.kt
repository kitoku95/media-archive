package io.github.kitoku95.mediaarchive.domain.usecase.category

import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Verifies the user-entered password against a `private` Category's stored salted hash.
 */
class VerifyCategoryPasswordUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, password: String): Boolean {
        return categoryRepository.verifyPassword(categoryId = categoryId, password = password)
    }
}
