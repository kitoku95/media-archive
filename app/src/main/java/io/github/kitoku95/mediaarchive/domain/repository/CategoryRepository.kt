package io.github.kitoku95.mediaarchive.domain.repository

import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for managing Categories, default `Main` seeding, and password verification.
 */
interface CategoryRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategoryById(categoryId: Long): Category?

    suspend fun ensureDefaultCategoryExists()

    suspend fun createCategory(
        name: String,
        categoryType: CategoryType,
        accessMode: AccessMode,
        password: String?
    ): Result<Category>

    suspend fun verifyPassword(categoryId: Long, password: String): Boolean

    suspend fun deleteCategory(categoryId: Long): Result<Unit>
}
