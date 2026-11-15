package io.github.kitoku95.mediaarchive.feature.categorydetail

import androidx.compose.runtime.Immutable
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType

/**
 * Immutable UI state for the Category Detail screen.
 */
@Immutable
data class CategoryDetailUiState(
    val categoryId: Long = 0L,
    val categoryName: String = "",
    val isPrivate: Boolean = false,
    val selectedTab: MediaType = MediaType.IMAGE,
    val images: List<MediaItem> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val isBrowseSheetVisible: Boolean = false
)

/**
 * User events emitted from [CategoryDetailScreen].
 */
sealed interface CategoryDetailUiEvent {
    data object OnBackClicked : CategoryDetailUiEvent
    data class OnTabSelected(val mediaType: MediaType) : CategoryDetailUiEvent
    data object OnBrowseStorageClicked : CategoryDetailUiEvent
    data object OnDismissBrowseSheet : CategoryDetailUiEvent
    data object OnSelectFilesClicked : CategoryDetailUiEvent
    data object OnSelectFolderClicked : CategoryDetailUiEvent
    data class OnMediaFilesSelected(val uris: List<String>) : CategoryDetailUiEvent
    data class OnFolderSelected(val treeUri: String) : CategoryDetailUiEvent
    data class OnImageClicked(val mediaId: Long, val index: Int) : CategoryDetailUiEvent
    data class OnVideoClicked(val mediaId: Long, val index: Int) : CategoryDetailUiEvent
}

/**
 * One-time side effects emitted by [CategoryDetailViewModel].
 */
sealed interface CategoryDetailUiEffect {
    data object NavigateBack : CategoryDetailUiEffect
    data object LaunchFilePicker : CategoryDetailUiEffect
    data object LaunchFolderPicker : CategoryDetailUiEffect
    data class NavigateToImageViewer(val categoryId: Long, val initialIndex: Int) : CategoryDetailUiEffect
    data class NavigateToVideoPlayer(val categoryId: Long, val initialIndex: Int) : CategoryDetailUiEffect
    data class ShowImportSuccess(val importedCount: Int) : CategoryDetailUiEffect
    data object ShowImportEmpty : CategoryDetailUiEffect
    data object ShowImportError : CategoryDetailUiEffect
}
