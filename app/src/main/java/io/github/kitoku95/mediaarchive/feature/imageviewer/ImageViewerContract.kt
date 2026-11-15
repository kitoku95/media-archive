package io.github.kitoku95.mediaarchive.feature.imageviewer

import androidx.compose.runtime.Immutable
import io.github.kitoku95.mediaarchive.domain.model.MediaItem

/**
 * Immutable UI state for [ImageViewerScreen].
 */
@Immutable
data class ImageViewerUiState(
    val categoryId: Long = 0L,
    val images: List<MediaItem> = emptyList(),
    val initialIndex: Int = 0,
    val isPrivate: Boolean = false,
    val isOverlayVisible: Boolean = true,
    val isLoading: Boolean = true,
    val deleteConfirmationItem: MediaItem? = null
)

/**
 * User and media events emitted from [ImageViewerScreen].
 */
sealed interface ImageViewerUiEvent {
    data object OnBackClicked : ImageViewerUiEvent
    data object OnToggleOverlay : ImageViewerUiEvent
    data class OnMediaFileMissing(val mediaItem: MediaItem) : ImageViewerUiEvent
    data class OnDeleteClicked(val mediaItem: MediaItem) : ImageViewerUiEvent
    data object OnDismissDeleteDialog : ImageViewerUiEvent
    data object OnConfirmDelete : ImageViewerUiEvent
}

/**
 * One-time side effects emitted by [ImageViewerViewModel].
 */
sealed interface ImageViewerUiEffect {
    data object NavigateBack : ImageViewerUiEffect
    data class ShowFileMissingMessage(
        val displayName: String,
        val shouldNavigateBack: Boolean
    ) : ImageViewerUiEffect
    data class ShowDeleteSuccess(
        val displayName: String,
        val shouldNavigateBack: Boolean
    ) : ImageViewerUiEffect
    data object ShowDeleteError : ImageViewerUiEffect
}
