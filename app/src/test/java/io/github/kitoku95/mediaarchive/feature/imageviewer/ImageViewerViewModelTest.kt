package io.github.kitoku95.mediaarchive.feature.imageviewer

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import io.github.kitoku95.mediaarchive.domain.usecase.media.DeleteMediaItemUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeImageMediaRepository
    private lateinit var viewModel: ImageViewerViewModel

    private val imageOne = MediaItem(
        id = 1L,
        categoryId = 10L,
        mediaType = MediaType.IMAGE,
        contentUri = "content://media/image/1",
        thumbnailPath = "/tmp/thumb1.webp",
        displayName = "photo1.jpg",
        mimeType = "image/jpeg",
        durationMs = null,
        sizeBytes = 1024L,
        addedAt = 100L
    )

    private val imageTwo = MediaItem(
        id = 2L,
        categoryId = 10L,
        mediaType = MediaType.IMAGE,
        contentUri = "content://media/image/2",
        thumbnailPath = "/tmp/thumb2.webp",
        displayName = "photo2.jpg",
        mimeType = "image/jpeg",
        durationMs = null,
        sizeBytes = 2048L,
        addedAt = 200L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeImageMediaRepository(listOf(imageOne, imageTwo))
        viewModel = ImageViewerViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "categoryId" to 10L,
                    "initialIndex" to 1,
                    "isPrivate" to true
                )
            ),
            getMediaByCategoryUseCase = GetMediaByCategoryUseCase(fakeRepository),
            deleteMediaItemUseCase = DeleteMediaItemUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState observes category images and initialIndex`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(10L, loaded.categoryId)
            assertEquals(1, loaded.initialIndex)
            assertTrue(loaded.isPrivate)
            assertEquals(listOf(imageOne, imageTwo), loaded.images)
            assertTrue(loaded.isOverlayVisible)
        }
    }

    @Test
    fun `onToggleOverlay toggles overlay visibility`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            val loaded = awaitItem()
            assertTrue(loaded.isOverlayVisible)

            viewModel.onEvent(ImageViewerUiEvent.OnToggleOverlay)
            assertFalse(awaitItem().isOverlayVisible)

            viewModel.onEvent(ImageViewerUiEvent.OnToggleOverlay)
            assertTrue(awaitItem().isOverlayVisible)
        }
    }

    @Test
    fun `onBackClicked emits NavigateBack`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(ImageViewerUiEvent.OnBackClicked)
            assertEquals(ImageViewerUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `onMediaFileMissing deletes entry and notifies user`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(ImageViewerUiEvent.OnMediaFileMissing(imageOne))
            advanceUntilIdle()

            assertEquals(listOf(1L), fakeRepository.deletedIds)
            val updated = awaitItem()
            assertEquals(listOf(imageTwo), updated.images)
        }

        viewModel.uiEffect.test {
            assertEquals(
                ImageViewerUiEffect.ShowFileMissingMessage(
                    displayName = "photo1.jpg",
                    shouldNavigateBack = false
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `onMediaFileMissing on last image requests navigation back`() = runTest {
        fakeRepository.setItems(listOf(imageOne))
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(ImageViewerUiEvent.OnMediaFileMissing(imageOne))
            advanceUntilIdle()

            assertEquals(emptyList<MediaItem>(), awaitItem().images)
        }

        viewModel.uiEffect.test {
            assertEquals(
                ImageViewerUiEffect.ShowFileMissingMessage(
                    displayName = "photo1.jpg",
                    shouldNavigateBack = true
                ),
                awaitItem()
            )
        }
    }

    private class FakeImageMediaRepository(
        initialItems: List<MediaItem>
    ) : MediaRepository {
        private val itemsFlow = MutableStateFlow(initialItems)
        val deletedIds = mutableListOf<Long>()

        fun setItems(items: List<MediaItem>) {
            itemsFlow.value = items
        }

        override fun observeMediaByCategoryAndType(
            categoryId: Long,
            mediaType: MediaType
        ): Flow<List<MediaItem>> {
            return itemsFlow.map { list ->
                list.filter { it.categoryId == categoryId && it.mediaType == mediaType }
            }
        }

        override suspend fun importSelectedMedia(
            categoryId: Long,
            categoryType: CategoryType,
            uriStrings: List<String>
        ): Result<ImportResult> = Result.success(ImportResult(0, 0))

        override suspend fun importFolderMedia(
            categoryId: Long,
            categoryType: CategoryType,
            treeUriString: String
        ): Result<ImportResult> = Result.success(ImportResult(0, 0))

        override suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit> {
            deletedIds.add(mediaItemId)
            itemsFlow.value = itemsFlow.value.filterNot { it.id == mediaItemId }
            return Result.success(Unit)
        }

        override suspend fun isMediaAccessible(contentUri: String): Boolean = false
    }

    @Test
    fun `manual delete from image viewer removes item and navigates back when last image`() = runTest {
        fakeRepository.setItems(listOf(imageOne))

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(ImageViewerUiEvent.OnDeleteClicked(imageOne))
            assertEquals(imageOne, awaitItem().deleteConfirmationItem)

            viewModel.onEvent(ImageViewerUiEvent.OnConfirmDelete)
            advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertEquals(null, updated.deleteConfirmationItem)
            assertEquals(emptyList<MediaItem>(), updated.images)
        }

        viewModel.uiEffect.test {
            assertEquals(
                ImageViewerUiEffect.ShowDeleteSuccess(
                    displayName = "photo1.jpg",
                    shouldNavigateBack = true
                ),
                awaitItem()
            )
        }
    }
}
