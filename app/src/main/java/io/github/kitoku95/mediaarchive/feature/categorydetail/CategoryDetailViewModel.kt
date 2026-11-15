package io.github.kitoku95.mediaarchive.feature.categorydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.usecase.media.GetMediaByCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.ImportFolderMediaUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.media.ImportSelectedMediaUseCase
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
 * Manages the state of [CategoryDetailScreen], including Image/Video tab selection
 * and manual SAF file/folder media ingestion.
 */
@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMediaByCategoryUseCase: GetMediaByCategoryUseCase,
    private val importSelectedMediaUseCase: ImportSelectedMediaUseCase,
    private val importFolderMediaUseCase: ImportFolderMediaUseCase
) : ViewModel() {

    private val route: AppDestination.CategoryDetail = if (savedStateHandle.contains("categoryId")) {
        AppDestination.CategoryDetail(
            categoryId = savedStateHandle.get<Long>("categoryId") ?: 0L,
            categoryName = savedStateHandle.get<String>("categoryName").orEmpty(),
            isPrivate = savedStateHandle.get<Boolean>("isPrivate") ?: false
        )
    } else {
        savedStateHandle.toRoute<AppDestination.CategoryDetail>()
    }
    private val categoryId: Long = route.categoryId

    private data class LocalUiState(
        val selectedTab: MediaType = MediaType.IMAGE,
        val isImporting: Boolean = false,
        val isBrowseSheetVisible: Boolean = false
    )

    private val localUiState = MutableStateFlow(LocalUiState())

    private val _uiEffect = Channel<CategoryDetailUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    val uiState: StateFlow<CategoryDetailUiState> = combine(
        getMediaByCategoryUseCase(categoryId, MediaType.IMAGE),
        getMediaByCategoryUseCase(categoryId, MediaType.VIDEO),
        localUiState
    ) { images, videos, local ->
        CategoryDetailUiState(
            categoryId = categoryId,
            categoryName = route.categoryName,
            isPrivate = route.isPrivate,
            selectedTab = local.selectedTab,
            images = images,
            videos = videos,
            isLoading = false,
            isImporting = local.isImporting,
            isBrowseSheetVisible = local.isBrowseSheetVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryDetailUiState(
            categoryId = categoryId,
            categoryName = route.categoryName,
            isPrivate = route.isPrivate,
            isLoading = true
        )
    )

    fun onEvent(event: CategoryDetailUiEvent) {
        when (event) {
            CategoryDetailUiEvent.OnBackClicked -> {
                _uiEffect.trySend(CategoryDetailUiEffect.NavigateBack)
            }

            is CategoryDetailUiEvent.OnTabSelected -> {
                localUiState.update { it.copy(selectedTab = event.mediaType) }
            }

            CategoryDetailUiEvent.OnBrowseStorageClicked -> {
                localUiState.update { it.copy(isBrowseSheetVisible = true) }
            }

            CategoryDetailUiEvent.OnDismissBrowseSheet -> {
                localUiState.update { it.copy(isBrowseSheetVisible = false) }
            }

            CategoryDetailUiEvent.OnSelectFilesClicked -> {
                localUiState.update { it.copy(isBrowseSheetVisible = false) }
                _uiEffect.trySend(CategoryDetailUiEffect.LaunchFilePicker)
            }

            CategoryDetailUiEvent.OnSelectFolderClicked -> {
                localUiState.update { it.copy(isBrowseSheetVisible = false) }
                _uiEffect.trySend(CategoryDetailUiEffect.LaunchFolderPicker)
            }

            is CategoryDetailUiEvent.OnMediaFilesSelected -> {
                importFiles(event.uris)
            }

            is CategoryDetailUiEvent.OnFolderSelected -> {
                importFolder(event.treeUri)
            }

            is CategoryDetailUiEvent.OnImageClicked -> {
                _uiEffect.trySend(
                    CategoryDetailUiEffect.NavigateToImageViewer(
                        categoryId = categoryId,
                        initialIndex = event.index
                    )
                )
            }

            is CategoryDetailUiEvent.OnVideoClicked -> {
                _uiEffect.trySend(
                    CategoryDetailUiEffect.NavigateToVideoPlayer(
                        categoryId = categoryId,
                        initialIndex = event.index
                    )
                )
            }
        }
    }

    private fun importFiles(uris: List<String>) {
        if (uris.isEmpty() || localUiState.value.isImporting) return
        viewModelScope.launch {
            localUiState.update { it.copy(isImporting = true) }
            importSelectedMediaUseCase(categoryId = categoryId, uriStrings = uris)
                .onSuccess { count -> emitImportCount(count) }
                .onFailure { _uiEffect.send(CategoryDetailUiEffect.ShowImportError) }
            localUiState.update { it.copy(isImporting = false) }
        }
    }

    private fun importFolder(treeUri: String) {
        if (treeUri.isBlank() || localUiState.value.isImporting) return
        viewModelScope.launch {
            localUiState.update { it.copy(isImporting = true) }
            importFolderMediaUseCase(categoryId = categoryId, treeUriString = treeUri)
                .onSuccess { count -> emitImportCount(count) }
                .onFailure { _uiEffect.send(CategoryDetailUiEffect.ShowImportError) }
            localUiState.update { it.copy(isImporting = false) }
        }
    }

    private suspend fun emitImportCount(count: Int) {
        if (count > 0) {
            _uiEffect.send(CategoryDetailUiEffect.ShowImportSuccess(count))
        } else {
            _uiEffect.send(CategoryDetailUiEffect.ShowImportEmpty)
        }
    }
}
