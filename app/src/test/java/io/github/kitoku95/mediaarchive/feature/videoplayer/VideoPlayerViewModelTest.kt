package io.github.kitoku95.mediaarchive.feature.videoplayer

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import io.github.kitoku95.mediaarchive.domain.usecase.media.DeleteMediaItemUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.IsMediaAccessibleUseCase
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
class VideoPlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeVideoMediaRepository
    private lateinit var viewModel: VideoPlayerViewModel

    private val videoOne = MediaItem(
        id = 11L,
        categoryId = 20L,
        mediaType = MediaType.VIDEO,
        contentUri = "content://media/video/11",
        thumbnailPath = "/tmp/vthumb1.webp",
        displayName = "clip1.mp4",
        mimeType = "video/mp4",
        durationMs = 15_000L,
        sizeBytes = 4096L,
        addedAt = 100L
    )

    private val videoTwo = MediaItem(
        id = 12L,
        categoryId = 20L,
        mediaType = MediaType.VIDEO,
        contentUri = "content://media/video/12",
        thumbnailPath = "/tmp/vthumb2.webp",
        displayName = "clip2.mp4",
        mimeType = "video/mp4",
        durationMs = 30_000L,
        sizeBytes = 8192L,
        addedAt = 200L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeVideoMediaRepository(listOf(videoOne, videoTwo))
        viewModel = VideoPlayerViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "categoryId" to 20L,
                    "initialIndex" to 1,
                    "isPrivate" to true
                )
            ),
            getMediaByCategoryUseCase = GetMediaByCategoryUseCase(fakeRepository),
            deleteMediaItemUseCase = DeleteMediaItemUseCase(fakeRepository),
            isMediaAccessibleUseCase = IsMediaAccessibleUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState observes category videos and initialIndex`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(20L, loaded.categoryId)
            assertEquals(1, loaded.initialIndex)
            assertTrue(loaded.isPrivate)
            assertEquals(listOf(videoOne, videoTwo), loaded.videos)
        }
    }

    @Test
    fun `onBackClicked emits NavigateBack`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(VideoPlayerUiEvent.OnBackClicked)
            assertEquals(VideoPlayerUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `onMediaFileMissing deletes entry and emits notification`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(VideoPlayerUiEvent.OnMediaFileMissing(11L))
            advanceUntilIdle()

            assertEquals(listOf(11L), fakeRepository.deletedIds)
            assertEquals(listOf(videoTwo), awaitItem().videos)
        }

        viewModel.uiEffect.test {
            assertEquals(
                VideoPlayerUiEffect.ShowFileMissingMessage(
                    displayName = "clip1.mp4",
                    shouldNavigateBack = false
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `onMediaFileMissing on last video requests navigation back`() = runTest {
        fakeRepository.setItems(listOf(videoOne))
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(VideoPlayerUiEvent.OnMediaFileMissing(11L))
            advanceUntilIdle()

            assertEquals(emptyList<MediaItem>(), awaitItem().videos)
        }

        viewModel.uiEffect.test {
            assertEquals(
                VideoPlayerUiEffect.ShowFileMissingMessage(
                    displayName = "clip1.mp4",
                    shouldNavigateBack = true
                ),
                awaitItem()
            )
        }
    }

    private class FakeVideoMediaRepository(
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
            uriStrings: List<String>
        ): Result<Int> = Result.success(0)

        override suspend fun importFolderMedia(
            categoryId: Long,
            treeUriString: String
        ): Result<Int> = Result.success(0)

        override suspend fun deleteMediaItem(mediaItemId: Long): Result<Unit> {
            deletedIds.add(mediaItemId)
            itemsFlow.value = itemsFlow.value.filterNot { it.id == mediaItemId }
            return Result.success(Unit)
        }

        var isAccessibleResult: Boolean = false

        override suspend fun isMediaAccessible(contentUri: String): Boolean = isAccessibleResult
    }

    @Test
    fun `playback error on accessible video shows error without deleting from SQLite`() = runTest {
        fakeRepository.isAccessibleResult = true

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(VideoPlayerUiEvent.OnMediaFileMissing(11L))
            advanceUntilIdle()
            expectNoEvents()
        }

        assertEquals(emptyList<Long>(), fakeRepository.deletedIds)
        viewModel.uiEffect.test {
            assertEquals(
                VideoPlayerUiEffect.ShowPlaybackError(displayName = "clip1.mp4"),
                awaitItem()
            )
        }
    }
}
