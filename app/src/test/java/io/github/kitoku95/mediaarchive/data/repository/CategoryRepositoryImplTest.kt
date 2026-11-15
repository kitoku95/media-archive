package io.github.kitoku95.mediaarchive.data.repository

import io.github.kitoku95.mediaarchive.core.security.PasswordHasher
import io.github.kitoku95.mediaarchive.data.local.dao.CategoryDao
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryRepositoryImplTest {

    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val mediaItemDao: MediaItemDao = mockk(relaxed = true)
    private val passwordHasher = PasswordHasher()
    private val thumbnailStorageManager: ThumbnailStorageManager = mockk(relaxed = true)

    private val repository = CategoryRepositoryImpl(
        categoryDao = categoryDao,
        mediaItemDao = mediaItemDao,
        passwordHasher = passwordHasher,
        thumbnailStorageManager = thumbnailStorageManager
    )

    @Test
    fun `ensureDefaultCategoryExists seeds Main PUBLIC category when missing`() = runTest {
        coEvery { categoryDao.hasDefaultCategory() } returns false
        val entitySlot = slot<CategoryEntity>()
        coEvery { categoryDao.insertDefaultCategoryIfMissing(capture(entitySlot)) } returns 1L

        repository.ensureDefaultCategoryExists()

        assertEquals("Main", entitySlot.captured.name)
        assertEquals("PUBLIC", entitySlot.captured.accessMode)
        assertTrue(entitySlot.captured.isDefault)
        assertNull(entitySlot.captured.passwordHash)
        assertNull(entitySlot.captured.passwordSalt)
    }

    @Test
    fun `deleteCategory rejects deleting default Main category`() = runTest {
        val mainEntity = CategoryEntity(
            id = 1L,
            name = "Main",
            accessMode = "PUBLIC",
            passwordHash = null,
            passwordSalt = null,
            isDefault = true,
            createdAt = 1000L
        )
        coEvery { categoryDao.getCategoryById(1L) } returns mainEntity

        val result = repository.deleteCategory(1L)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { categoryDao.deleteCustomCategoryById(any()) }
    }

    @Test
    fun `deleteCategory deletes custom category and cleans up thumbnails`() = runTest {
        val customEntity = CategoryEntity(
            id = 2L,
            name = "Travel",
            accessMode = "PUBLIC",
            passwordHash = null,
            passwordSalt = null,
            isDefault = false,
            createdAt = 2000L
        )
        val thumbPaths = listOf("/files/thumbnails/1.webp", "/files/thumbnails/2.webp")
        coEvery { categoryDao.getCategoryById(2L) } returns customEntity
        coEvery { mediaItemDao.getThumbnailPathsByCategory(2L) } returns thumbPaths

        val result = repository.deleteCategory(2L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { categoryDao.deleteCustomCategoryById(2L) }
        coVerify(exactly = 1) { thumbnailStorageManager.deleteThumbnails(thumbPaths) }
    }

    @Test
    fun `createCategory with PRIVATE mode accepts 1-digit password and hashes it`() = runTest {
        coEvery { categoryDao.getCategoryByName("Vault") } returns null
        val entitySlot = slot<CategoryEntity>()
        coEvery { categoryDao.insertCategory(capture(entitySlot)) } returns 3L

        val result = repository.createCategory(
            name = "Vault",
            accessMode = AccessMode.PRIVATE,
            password = "1"
        )

        assertTrue(result.isSuccess)
        val saved = entitySlot.captured
        assertEquals("PRIVATE", saved.accessMode)
        assertNotNull(saved.passwordHash)
        assertNotNull(saved.passwordSalt)
        assertNotEquals("1", saved.passwordHash)

        coEvery { categoryDao.getCategoryById(3L) } returns saved.copy(id = 3L)
        assertTrue(repository.verifyPassword(3L, "1"))
        assertFalse(repository.verifyPassword(3L, "2"))
    }
}
