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
    val isOverlayVisible: Boolean = true,
    val isLoading: Boolean = true
)

/**
 * User and media events emitted from [ImageViewerScreen].
 */
sealed interface ImageViewerUiEvent {
    data object OnBackClicked : ImageViewerUiEvent
    data object OnToggleOverlay : ImageViewerUiEvent
    data class OnMediaFileMissing(val mediaItem: MediaItem) : ImageViewerUiEvent
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
}
