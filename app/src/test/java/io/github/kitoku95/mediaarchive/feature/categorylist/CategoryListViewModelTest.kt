package io.github.kitoku95.mediaarchive.feature.categorylist

import app.cash.turbine.test
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import io.github.kitoku95.mediaarchive.domain.usecase.category.CreateCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.category.GetCategoriesUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.category.VerifyCategoryPasswordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var viewModel: CategoryListViewModel

    private val mainCategory = Category(
        id = 1L,
        name = "Main",
        accessMode = AccessMode.PUBLIC,
        isDefault = true,
        createdAt = 100L
    )

    private val privateCategory = Category(
        id = 2L,
        name = "Vault",
        accessMode = AccessMode.PRIVATE,
        isDefault = false,
        createdAt = 200L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCategoryRepository(
            initialCategories = listOf(mainCategory, privateCategory),
            passwords = mutableMapOf(2L to "7")
        )
        viewModel = CategoryListViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(fakeRepository),
            createCategoryUseCase = CreateCategoryUseCase(fakeRepository),
            verifyCategoryPasswordUseCase = VerifyCategoryPasswordUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits categories with Main first`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(listOf(mainCategory, privateCategory), loaded.categories)
        }
    }

    @Test
    fun `clicking public category navigates immediately`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(CategoryListUiEvent.OnCategoryClicked(mainCategory))
            advanceUntilIdle()
            assertEquals(
                CategoryListUiEffect.NavigateToCategoryDetail(mainCategory),
                awaitItem()
            )
        }
    }

    @Test
    fun `clicking private category opens password prompt and unlocks on matching 1-digit password`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loaded

            viewModel.onEvent(CategoryListUiEvent.OnCategoryClicked(privateCategory))
            val prompted = awaitItem()
            assertEquals(privateCategory, prompted.passwordPrompt?.category)

            // Wrong password
            viewModel.onEvent(CategoryListUiEvent.OnPasswordPromptChanged("x"))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnConfirmPasswordPrompt)
            advanceUntilIdle()
            val errorState = expectMostRecentItem()
            assertTrue(errorState.passwordPrompt?.isError == true)

            // Correct 1-digit password
            viewModel.onEvent(CategoryListUiEvent.OnPasswordPromptChanged("7"))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnConfirmPasswordPrompt)
            advanceUntilIdle()
            val unlockedState = expectMostRecentItem()
            assertNull(unlockedState.passwordPrompt)
        }
    }

    @Test
    fun `creating private category with 1-character password succeeds`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loaded

            viewModel.onEvent(CategoryListUiEvent.OnAddCategoryClicked)
            assertNotNull(awaitItem().createDialog)

            viewModel.onEvent(CategoryListUiEvent.OnCreateNameChanged("Diary"))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnCreateAccessModeChanged(AccessMode.PRIVATE))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnCreatePasswordChanged("a"))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnConfirmCreateCategory)
            advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertNull(updated.createDialog)
            assertTrue(updated.categories.any { it.name == "Diary" && it.accessMode == AccessMode.PRIVATE })
        }
    }

    @Test
    fun `creating category with blank name shows validation error`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loaded

            viewModel.onEvent(CategoryListUiEvent.OnAddCategoryClicked)
            awaitItem()

            viewModel.onEvent(CategoryListUiEvent.OnCreateNameChanged("   "))
            awaitItem()
            viewModel.onEvent(CategoryListUiEvent.OnConfirmCreateCategory)

            val errorState = awaitItem()
            assertEquals(CreateCategoryError.BLANK_NAME, errorState.createDialog?.error)
        }
    }

    private class FakeCategoryRepository(
        initialCategories: List<Category>,
        private val passwords: MutableMap<Long, String>
    ) : CategoryRepository {
        private val categoriesFlow = MutableStateFlow(initialCategories)
        private var nextId = (initialCategories.maxOfOrNull { it.id } ?: 0L) + 1L

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow

        override suspend fun getCategoryById(categoryId: Long): Category? {
            return categoriesFlow.value.firstOrNull { it.id == categoryId }
        }

        override suspend fun ensureDefaultCategoryExists() = Unit

        override suspend fun createCategory(
            name: String,
            accessMode: AccessMode,
            password: String?
        ): Result<Category> {
            val trimmed = name.trim()
            if (categoriesFlow.value.any { it.name.equals(trimmed, ignoreCase = true) }) {
                return Result.failure(IllegalArgumentException("Category '$trimmed' already exists."))
            }
            val created = Category(
                id = nextId++,
                name = trimmed,
                accessMode = accessMode,
                isDefault = false,
                createdAt = System.currentTimeMillis()
            )
            if (accessMode == AccessMode.PRIVATE && password != null) {
                passwords[created.id] = password
            }
            categoriesFlow.value = categoriesFlow.value + created
            return Result.success(created)
        }

        override suspend fun verifyPassword(categoryId: Long, password: String): Boolean {
            return passwords[categoryId] == password
        }

        override suspend fun deleteCategory(categoryId: Long): Result<Unit> = Result.success(Unit)
    }
}
