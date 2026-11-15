package io.github.kitoku95.mediaarchive.feature.imageviewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.usecase.media.DeleteMediaItemUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import io.github.kitoku95.mediaarchive.navigation.AppDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages state for [ImageViewerScreen], observing category images and cleaning up missing entries.
 */
@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMediaByCategoryUseCase: GetMediaByCategoryUseCase,
    private val deleteMediaItemUseCase: DeleteMediaItemUseCase
) : ViewModel() {

    private val route: AppDestination.ImageViewer = if (savedStateHandle.contains("categoryId")) {
        AppDestination.ImageViewer(
            categoryId = savedStateHandle.get<Number>("categoryId")?.toLong() ?: 0L,
            initialIndex = savedStateHandle.get<Number>("initialIndex")?.toInt() ?: 0,
            isPrivate = savedStateHandle.get<Boolean>("isPrivate") ?: false
        )
    } else {
        savedStateHandle.toRoute<AppDestination.ImageViewer>()
    }

    private val isOverlayVisible = MutableStateFlow(true)
    private val deleteConfirmationItem = MutableStateFlow<MediaItem?>(null)
    private val handledMissingIds = mutableSetOf<Long>()

    private val _uiEffect = Channel<ImageViewerUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    val uiState: StateFlow<ImageViewerUiState> = combine(
        getMediaByCategoryUseCase(route.categoryId, MediaType.IMAGE),
        isOverlayVisible,
        deleteConfirmationItem
    ) { images, overlayVisible, deleteCandidate ->
        ImageViewerUiState(
            categoryId = route.categoryId,
            images = images,
            initialIndex = route.initialIndex,
            isPrivate = route.isPrivate,
            isOverlayVisible = overlayVisible,
            isLoading = false,
            deleteConfirmationItem = deleteCandidate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ImageViewerUiState(
            categoryId = route.categoryId,
            initialIndex = route.initialIndex,
            isPrivate = route.isPrivate,
            isLoading = true
        )
    )

    fun onEvent(event: ImageViewerUiEvent) {
        when (event) {
            ImageViewerUiEvent.OnBackClicked -> {
                _uiEffect.trySend(ImageViewerUiEffect.NavigateBack)
            }

            ImageViewerUiEvent.OnToggleOverlay -> {
                isOverlayVisible.update { !it }
            }

            is ImageViewerUiEvent.OnMediaFileMissing -> {
                handleMissingMedia(event)
            }

            is ImageViewerUiEvent.OnDeleteClicked -> {
                deleteConfirmationItem.value = event.mediaItem
            }

            ImageViewerUiEvent.OnDismissDeleteDialog -> {
                deleteConfirmationItem.value = null
            }

            ImageViewerUiEvent.OnConfirmDelete -> {
                confirmDeleteMedia()
            }
        }
    }

    private fun confirmDeleteMedia() {
        val item = deleteConfirmationItem.value ?: return
        deleteConfirmationItem.value = null
        viewModelScope.launch {
            val remainingCount = uiState.value.images.count { it.id != item.id }
            deleteMediaItemUseCase(item.id)
                .onSuccess {
                    _uiEffect.send(
                        ImageViewerUiEffect.ShowDeleteSuccess(
                            displayName = item.displayName,
                            shouldNavigateBack = remainingCount == 0
                        )
                    )
                }
                .onFailure {
                    _uiEffect.send(ImageViewerUiEffect.ShowDeleteError)
                }
        }
    }

    private fun handleMissingMedia(event: ImageViewerUiEvent.OnMediaFileMissing) {
        val item = event.mediaItem
        if (!handledMissingIds.add(item.id)) return

        viewModelScope.launch {
            val remainingCount = uiState.value.images.count { it.id != item.id }
            deleteMediaItemUseCase(item.id)
            _uiEffect.send(
                ImageViewerUiEffect.ShowFileMissingMessage(
                    displayName = item.displayName,
                    shouldNavigateBack = remainingCount == 0
                )
            )
        }
    }
}
