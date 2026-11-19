package io.github.kitoku95.mediaarchive.feature.categorydetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import io.github.kitoku95.mediaarchive.domain.usecase.media.DeleteMediaItemUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.ImportFolderMediaUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.ImportSelectedMediaUseCase
import io.github.kitoku95.mediaarchive.feature.categorydetail.component.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class CategoryDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMediaRepository
    private lateinit var viewModel: CategoryDetailViewModel

    private val sampleImage = MediaItem(
        id = 1L,
        categoryId = 10L,
        mediaType = MediaType.IMAGE,
        contentUri = "content://media/image/1",
        thumbnailPath = "/tmp/thumb1.webp",
        displayName = "beach.jpg",
        mimeType = "image/jpeg",
        durationMs = null,
        sizeBytes = 2048L,
        addedAt = 100L
    )

    private val sampleVideo = MediaItem(
        id = 2L,
        categoryId = 10L,
        mediaType = MediaType.VIDEO,
        contentUri = "content://media/video/2",
        thumbnailPath = "/tmp/thumb2.webp",
        displayName = "clip.mp4",
        mimeType = "video/mp4",
        durationMs = 65_000L,
        sizeBytes = 10_240L,
        addedAt = 200L
    )

    private val sampleDocument = MediaItem(
        id = 3L,
        categoryId = 10L,
        mediaType = MediaType.DOCUMENT,
        contentUri = "content://media/doc/3",
        thumbnailPath = "",
        displayName = "itinerary.pdf",
        mimeType = "application/pdf",
        durationMs = null,
        sizeBytes = 32_768L,
        addedAt = 300L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMediaRepository(
            initialItems = listOf(sampleImage, sampleVideo, sampleDocument)
        )
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryId" to 10L,
                "categoryName" to "Travel",
                "isPrivate" to true
            )
        )
        viewModel = CategoryDetailViewModel(
            savedStateHandle = savedStateHandle,
            getMediaByCategoryUseCase = GetMediaByCategoryUseCase(fakeRepository),
            importSelectedMediaUseCase = ImportSelectedMediaUseCase(fakeRepository),
            importFolderMediaUseCase = ImportFolderMediaUseCase(fakeRepository),
            deleteMediaItemUseCase = DeleteMediaItemUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads separated images, videos, and documents directly for category`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(10L, loaded.categoryId)
            assertEquals("Travel", loaded.categoryName)
            assertTrue(loaded.isPrivate)
            assertEquals(MediaType.IMAGE, loaded.selectedTab)
            assertEquals(listOf(sampleImage), loaded.images)
            assertEquals(listOf(sampleVideo), loaded.videos)
            assertEquals(listOf(sampleDocument), loaded.documents)
        }
    }

    @Test
    fun `selecting document tab updates selectedTab`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(CategoryDetailUiEvent.OnTabSelected(MediaType.DOCUMENT))
            assertEquals(MediaType.DOCUMENT, awaitItem().selectedTab)
        }
    }

    @Test
    fun `tapping document emits OpenDocumentExternally effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(CategoryDetailUiEvent.OnDocumentClicked(sampleDocument))
            assertEquals(
                CategoryDetailUiEffect.OpenDocumentExternally(
                    contentUri = sampleDocument.contentUri,
                    mimeType = sampleDocument.mimeType,
                    displayName = sampleDocument.displayName
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `importing selected files emits ShowImportResult and updates grid reactively`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.uiEffect.test {
                viewModel.onEvent(
                    CategoryDetailUiEvent.OnMediaFilesSelected(listOf("content://media/image/3"))
                )
                advanceUntilIdle()
                assertEquals(
                    CategoryDetailUiEffect.ShowImportResult(importedCount = 1, skippedDuplicateCount = 0),
                    awaitItem()
                )
            }

            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(2, viewModel.uiState.value.images.size)
    }

    @Test
    fun `importing folder emits error effect when repository fails`() = runTest {
        fakeRepository.shouldFailImport = true

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.uiEffect.test {
                viewModel.onEvent(
                    CategoryDetailUiEvent.OnFolderSelected("content://tree/folder")
                )
                advanceUntilIdle()
                assertEquals(CategoryDetailUiEffect.ShowImportError, awaitItem())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tapping image or video emits respective viewer navigation effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(CategoryDetailUiEvent.OnImageClicked(mediaId = 1L, index = 0))
            assertEquals(
                CategoryDetailUiEffect.NavigateToImageViewer(
                    categoryId = 10L,
                    initialIndex = 0,
                    isPrivate = true
                ),
                awaitItem()
            )

            viewModel.onEvent(CategoryDetailUiEvent.OnVideoClicked(mediaId = 2L, index = 0))
            assertEquals(
                CategoryDetailUiEffect.NavigateToVideoPlayer(
                    categoryId = 10L,
                    initialIndex = 0,
                    isPrivate = true
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `formatDuration formats mm ss and hh mm ss accurately`() {
        assertEquals("00:00", formatDuration(0L))
        assertEquals("01:05", formatDuration(65_000L))
        assertEquals("1:01:01", formatDuration(3_661_000L))
    }

    @Test
    fun `confirming delete media item removes it from grid and emits success effect`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(CategoryDetailUiEvent.OnDeleteMediaClicked(sampleImage))
            val confirming = awaitItem()
            assertEquals(sampleImage, confirming.deleteConfirmationItem)

            viewModel.onEvent(CategoryDetailUiEvent.OnConfirmDeleteMedia)
            advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertEquals(null, updated.deleteConfirmationItem)
            assertEquals(emptyList<MediaItem>(), updated.images)
        }

        viewModel.uiEffect.test {
            assertEquals(
                CategoryDetailUiEffect.ShowDeleteMediaSuccess(sampleImage.displayName),
                awaitItem()
            )
        }
    }

    private class FakeMediaRepository(
        initialItems: List<MediaItem> = emptyList()
    ) : MediaRepository {
        private val itemsFlow = MutableStateFlow(initialItems)
        private var nextId = 100L
        var shouldFailImport = false

        override fun observeMediaByCategoryAndType(
            categoryId: Long,
            mediaType: MediaType
        ): Flow<List<MediaItem>> {
            return itemsFlow.map { items ->
                items.filter { it.categoryId == categoryId && it.mediaType == mediaType }
            }
        }

        override suspend fun importSelectedMedia(
            categoryId: Long,
            uriStrings: List<String>
        ): Result<ImportResult> {
            if (shouldFailImport) {
                return Result.failure(IllegalStateException("Simulated SAF error"))
            }
            val newItems = uriStrings.map { uri ->
                MediaItem(
                    id = nextId++,
                    categoryId = categoryId,
                    mediaType = MediaType.IMAGE,
                    contentUri = uri,
                    thumbnailPath = "/tmp/thumb_$nextId.webp",
                    displayName = "imported_$nextId.jpg",
                    mimeType = "image/jpeg",
                    durationMs = null,
                    sizeBytes = 1024L,
                    addedAt = System.currentTimeMillis()
                )
            }
            itemsFlow.value = itemsFlow.value + newItems
            return Result.success(ImportResult(importedCount = newItems.size, skippedDuplicateCount = 0))
        }

        override suspend fun importFolderMedia(
            categoryId: Long,
            treeUriString: String
        ): Result<ImportResult> {
            if (shouldFailImport) {
                return Result.failure(IllegalStateException("Simulated folder error"))
            }
            return Result.success(ImportResult(0, 0))
        }

        override suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit> {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == mediaItemId }
            return Result.success(Unit)
        }

        override suspend fun isMediaAccessible(contentUri: String): Boolean = true
    }
}
