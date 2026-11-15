package io.github.kitoku95.mediaarchive.data.repository

import io.github.kitoku95.mediaarchive.core.security.PasswordHasher
import io.github.kitoku95.mediaarchive.data.local.MediaArchiveDatabase
import io.github.kitoku95.mediaarchive.data.local.dao.CategoryDao
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.data.mapper.toDomain
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val mediaItemDao: MediaItemDao,
    private val passwordHasher: PasswordHasher,
    private val thumbnailStorageManager: ThumbnailStorageManager
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> {
        return categoryDao.observeCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(categoryId: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)?.toDomain()
    }

    override suspend fun ensureDefaultCategoryExists() = withContext(Dispatchers.IO) {
        if (!categoryDao.hasDefaultCategory()) {
            categoryDao.insertDefaultCategoryIfMissing(
                CategoryEntity(
                    name = MediaArchiveDatabase.DEFAULT_CATEGORY_NAME,
                    accessMode = AccessMode.PUBLIC.name,
                    passwordHash = null,
                    passwordSalt = null,
                    isDefault = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun createCategory(
        name: String,
        accessMode: AccessMode,
        password: String?
    ): Result<Category> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedName = name.trim()
            require(trimmedName.isNotEmpty()) { "Category name must not be blank." }
            require(categoryDao.getCategoryByName(trimmedName) == null) {
                "Category '$trimmedName' already exists."
            }

            val hashedPassword = when (accessMode) {
                AccessMode.PUBLIC -> null
                AccessMode.PRIVATE -> {
                    require(!password.isNullOrEmpty()) {
                        "Password is required for private categories."
                    }
                    passwordHasher.hash(password)
                }
            }

            val entity = CategoryEntity(
                name = trimmedName,
                accessMode = accessMode.name,
                passwordHash = hashedPassword?.hashBase64,
                passwordSalt = hashedPassword?.saltBase64,
                isDefault = false,
                createdAt = System.currentTimeMillis()
            )
            val insertedId = categoryDao.insertCategory(entity)
            entity.copy(id = insertedId).toDomain()
        }
    }

    override suspend fun verifyPassword(
        categoryId: Long,
        password: String
    ): Boolean = withContext(Dispatchers.IO) {
        val entity = categoryDao.getCategoryById(categoryId) ?: return@withContext false
        if (entity.accessMode == AccessMode.PUBLIC.name) {
            return@withContext true
        }
        val hash = entity.passwordHash ?: return@withContext false
        val salt = entity.passwordSalt ?: return@withContext false
        passwordHasher.verify(password = password, expectedHashBase64 = hash, saltBase64 = salt)
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = categoryDao.getCategoryById(categoryId)
                ?: throw IllegalArgumentException("Category not found.")
            if (entity.isDefault) {
                throw IllegalStateException("Default Main category cannot be deleted.")
            }

            val thumbnailPaths = mediaItemDao.getThumbnailPathsByCategory(categoryId)
            categoryDao.deleteCustomCategoryById(categoryId)
            thumbnailStorageManager.deleteThumbnails(thumbnailPaths)
        }
    }
}
