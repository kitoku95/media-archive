package io.github.kitoku95.mediaarchive.feature.videoplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.usecase.media.DeleteMediaItemUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.IsMediaAccessibleUseCase
import io.github.kitoku95.mediaarchive.navigation.AppDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages state for [VideoPlayerScreen], observing category videos and cleaning up missing entries.
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMediaByCategoryUseCase: GetMediaByCategoryUseCase,
    private val deleteMediaItemUseCase: DeleteMediaItemUseCase,
    private val isMediaAccessibleUseCase: IsMediaAccessibleUseCase
) : ViewModel() {

    private val route: AppDestination.VideoPlayer = if (savedStateHandle.contains("categoryId")) {
        AppDestination.VideoPlayer(
            categoryId = savedStateHandle.get<Number>("categoryId")?.toLong() ?: 0L,
            initialIndex = savedStateHandle.get<Number>("initialIndex")?.toInt() ?: 0,
            isPrivate = savedStateHandle.get<Boolean>("isPrivate") ?: false
        )
    } else {
        savedStateHandle.toRoute<AppDestination.VideoPlayer>()
    }

    private val handledMissingIds = mutableSetOf<Long>()

    private val _uiEffect = Channel<VideoPlayerUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    val uiState: StateFlow<VideoPlayerUiState> = getMediaByCategoryUseCase(
        categoryId = route.categoryId,
        mediaType = MediaType.VIDEO
    ).map { videos ->
        VideoPlayerUiState(
            categoryId = route.categoryId,
            videos = videos,
            initialIndex = route.initialIndex,
            isPrivate = route.isPrivate,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VideoPlayerUiState(
            categoryId = route.categoryId,
            initialIndex = route.initialIndex,
            isPrivate = route.isPrivate,
            isLoading = true
        )
    )

    fun onEvent(event: VideoPlayerUiEvent) {
        when (event) {
            VideoPlayerUiEvent.OnBackClicked -> {
                _uiEffect.trySend(VideoPlayerUiEffect.NavigateBack)
            }

            is VideoPlayerUiEvent.OnMediaFileMissing -> {
                handleMissingMedia(event.mediaItemId)
            }
        }
    }

    private fun handleMissingMedia(mediaItemId: Long) {
        if (!handledMissingIds.add(mediaItemId)) return

        viewModelScope.launch {
            val currentVideos = uiState.value.videos
            val missingItem = currentVideos.firstOrNull { it.id == mediaItemId }
            val isStillAccessible = missingItem != null && isMediaAccessibleUseCase(missingItem.contentUri)
            if (isStillAccessible) {
                _uiEffect.send(
                    VideoPlayerUiEffect.ShowPlaybackError(
                        displayName = missingItem?.displayName.orEmpty()
                    )
                )
                return@launch
            }

            val remainingCount = currentVideos.count { it.id != mediaItemId }
            deleteMediaItemUseCase(mediaItemId)
            _uiEffect.send(
                VideoPlayerUiEffect.ShowFileMissingMessage(
                    displayName = missingItem?.displayName.orEmpty(),
                    shouldNavigateBack = remainingCount == 0
                )
            )
        }
    }
}
