package io.github.kitoku95.mediaarchive.feature.videoplayer

import androidx.compose.runtime.Immutable
import io.github.kitoku95.mediaarchive.domain.model.MediaItem

/**
 * Immutable UI state for [VideoPlayerScreen].
 */
@Immutable
data class VideoPlayerUiState(
    val categoryId: Long = 0L,
    val videos: List<MediaItem> = emptyList(),
    val initialIndex: Int = 0,
    val isPrivate: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * User and playback events emitted from [VideoPlayerScreen].
 */
sealed interface VideoPlayerUiEvent {
    data object OnBackClicked : VideoPlayerUiEvent
    data class OnMediaFileMissing(val mediaItemId: Long) : VideoPlayerUiEvent
}

/**
 * One-time side effects emitted by [VideoPlayerViewModel].
 */
sealed interface VideoPlayerUiEffect {
    data object NavigateBack : VideoPlayerUiEffect
    data class ShowFileMissingMessage(
        val displayName: String,
        val shouldNavigateBack: Boolean
    ) : VideoPlayerUiEffect
    data class ShowPlaybackError(val displayName: String) : VideoPlayerUiEffect
}
