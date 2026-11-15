package io.github.kitoku95.mediaarchive.domain.usecase.category

import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryUseCasesTest {

    private val repository: CategoryRepository = mockk()
    private val getCategoriesUseCase = GetCategoriesUseCase(repository)
    private val createCategoryUseCase = CreateCategoryUseCase(repository)
    private val verifyCategoryPasswordUseCase = VerifyCategoryPasswordUseCase(repository)

    @Test
    fun `getCategoriesUseCase returns repository flow`() = runTest {
        val main = Category(1L, "Main", AccessMode.PUBLIC, isDefault = true, createdAt = 100L)
        every { repository.observeCategories() } returns flowOf(listOf(main))

        val result = getCategoriesUseCase().first()

        assertEquals(listOf(main), result)
    }

    @Test
    fun `createCategoryUseCase rejects blank name`() = runTest {
        val result = createCategoryUseCase("   ", AccessMode.PUBLIC, null)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.createCategory(any(), any(), any()) }
    }

    @Test
    fun `createCategoryUseCase rejects empty password for private category`() = runTest {
        val result = createCategoryUseCase("Secret", AccessMode.PRIVATE, "")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.createCategory(any(), any(), any()) }
    }

    @Test
    fun `createCategoryUseCase accepts 1-character and 1-digit passwords for private category`() = runTest {
        val created = Category(2L, "Secret", AccessMode.PRIVATE, isDefault = false, createdAt = 200L)
        coEvery { repository.createCategory("Secret", AccessMode.PRIVATE, "1") } returns Result.success(created)
        coEvery { repository.createCategory("Space", AccessMode.PRIVATE, " ") } returns Result.success(created.copy(id = 3L, name = "Space"))

        val digitResult = createCategoryUseCase("  Secret  ", AccessMode.PRIVATE, "1")
        val spaceResult = createCategoryUseCase("Space", AccessMode.PRIVATE, " ")

        assertTrue(digitResult.isSuccess)
        assertTrue(spaceResult.isSuccess)
        coVerify { repository.createCategory("Secret", AccessMode.PRIVATE, "1") }
        coVerify { repository.createCategory("Space", AccessMode.PRIVATE, " ") }
    }

    @Test
    fun `verifyCategoryPasswordUseCase delegates to repository`() = runTest {
        coEvery { repository.verifyPassword(2L, "a") } returns true
        coEvery { repository.verifyPassword(2L, "b") } returns false

        assertTrue(verifyCategoryPasswordUseCase(2L, "a"))
        assertFalse(verifyCategoryPasswordUseCase(2L, "b"))
    }
}
