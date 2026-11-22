package io.github.kitoku95.mediaarchive.feature.categorydetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.core.security.PrivateScreenProtectionEffect
import io.github.kitoku95.mediaarchive.data.storage.DocumentOpenOutcome
import io.github.kitoku95.mediaarchive.data.storage.ExternalDocumentOpener
import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.feature.categorydetail.component.BrowseStorageBottomSheet
import io.github.kitoku95.mediaarchive.feature.categorydetail.component.DocumentListItem
import io.github.kitoku95.mediaarchive.feature.categorydetail.component.MediaSquareGridItem

private val MEDIA_PICKER_MIMES = arrayOf(
    "image/*",
    "video/*"
)

private val DOCUMENT_PICKER_MIMES = arrayOf(
    "text/*",
    "application/pdf",
    "application/rtf",
    "application/json",
    "application/xml",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.oasis.opendocument.text",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.oasis.opendocument.spreadsheet",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.oasis.opendocument.presentation"
)

/**
 * Stateful route connecting [CategoryDetailViewModel], SAF file/folder pickers, and navigation.
 */
@Composable
fun CategoryDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToImageViewer: (categoryId: Long, initialIndex: Int, isPrivate: Boolean) -> Unit,
    onNavigateToVideoPlayer: (categoryId: Long, initialIndex: Int, isPrivate: Boolean) -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val documentOpener = remember { ExternalDocumentOpener() }

    PrivateScreenProtectionEffect(isPrivate = uiState.isPrivate)

    val openMultipleDocumentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(
                CategoryDetailUiEvent.OnMediaFilesSelected(uris.map { it.toString() })
            )
        }
    }

    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        treeUri?.let {
            viewModel.onEvent(CategoryDetailUiEvent.OnFolderSelected(it.toString()))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CategoryDetailUiEffect.NavigateBack -> onNavigateBack()
                is CategoryDetailUiEffect.LaunchFilePicker -> {
                    val mimeTypes = when (effect.categoryType) {
                        CategoryType.MEDIA -> MEDIA_PICKER_MIMES
                        CategoryType.DOCUMENT -> DOCUMENT_PICKER_MIMES
                    }
                    openMultipleDocumentsLauncher.launch(mimeTypes)
                }
                CategoryDetailUiEffect.LaunchFolderPicker -> {
                    openDocumentTreeLauncher.launch(null)
                }
                is CategoryDetailUiEffect.NavigateToImageViewer -> {
                    onNavigateToImageViewer(
                        effect.categoryId,
                        effect.initialIndex,
                        effect.isPrivate
                    )
                }
                is CategoryDetailUiEffect.NavigateToVideoPlayer -> {
                    onNavigateToVideoPlayer(
                        effect.categoryId,
                        effect.initialIndex,
                        effect.isPrivate
                    )
                }
                is CategoryDetailUiEffect.OpenDocumentExternally -> {
                    when (
                        documentOpener.openDocument(
                            context = context,
                            contentUri = effect.contentUri,
                            mimeType = effect.mimeType,
                            displayName = effect.displayName
                        )
                    ) {
                        DocumentOpenOutcome.Opened -> Unit
                        DocumentOpenOutcome.NoAppFound -> {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.document_open_no_app)
                            )
                        }
                        DocumentOpenOutcome.FileInaccessible -> {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.document_open_inaccessible)
                            )
                        }
                    }
                }
                is CategoryDetailUiEffect.ShowImportResult -> {
                    val message = when {
                        effect.importedCount > 0 && effect.skippedDuplicateCount > 0 ->
                            context.getString(
                                R.string.category_detail_import_with_duplicates,
                                effect.importedCount,
                                effect.skippedDuplicateCount
                            )
                        effect.importedCount > 0 ->
                            context.getString(
                                R.string.category_detail_import_success,
                                effect.importedCount
                            )
                        else ->
                            context.getString(
                                R.string.category_detail_import_all_duplicates,
                                effect.skippedDuplicateCount
                            )
                    }
                    snackbarHostState.showSnackbar(message)
                }
                CategoryDetailUiEffect.ShowImportEmpty -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.category_detail_import_empty)
                    )
                }
                CategoryDetailUiEffect.ShowImportError -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.category_detail_import_error)
                    )
                }
                is CategoryDetailUiEffect.ShowDeleteMediaSuccess -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.media_delete_success, effect.displayName)
                    )
                }
                CategoryDetailUiEffect.ShowDeleteMediaError -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.media_delete_error)
                    )
                }
            }
        }
    }

    CategoryDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    uiState: CategoryDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CategoryDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMediaCategory = uiState.categoryType == CategoryType.MEDIA
    val activeMediaTab = if (uiState.selectedTab == MediaType.VIDEO) MediaType.VIDEO else MediaType.IMAGE
    val selectedTabIndex = if (activeMediaTab == MediaType.VIDEO) 1 else 0
    val activeMediaItems = if (activeMediaTab == MediaType.VIDEO) uiState.videos else uiState.images

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.categoryName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.isPrivate) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = stringResource(R.string.category_mode_private),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CategoryDetailUiEvent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(CategoryDetailUiEvent.OnBrowseStorageClicked) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = stringResource(R.string.category_detail_btn_browse_storage))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isMediaCategory) {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = activeMediaTab == MediaType.IMAGE,
                        onClick = { onEvent(CategoryDetailUiEvent.OnTabSelected(MediaType.IMAGE)) },
                        text = {
                            Text(
                                text = "${stringResource(R.string.category_detail_tab_images)} (${uiState.images.size})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    Tab(
                        selected = activeMediaTab == MediaType.VIDEO,
                        onClick = { onEvent(CategoryDetailUiEvent.OnTabSelected(MediaType.VIDEO)) },
                        text = {
                            Text(
                                text = "${stringResource(R.string.category_detail_tab_videos)} (${uiState.videos.size})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            if (uiState.isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(
                        if (isMediaCategory) {
                            R.string.category_detail_importing
                        } else {
                            R.string.category_detail_importing_documents
                        }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !isMediaCategory -> {
                    if (uiState.documents.isEmpty()) {
                        EmptyMediaTabState(selectedTab = MediaType.DOCUMENT)
                    } else {
                        DocumentList(
                            items = uiState.documents,
                            onDocumentClick = { item ->
                                onEvent(CategoryDetailUiEvent.OnDocumentClicked(item))
                            },
                            onDocumentLongClick = { item ->
                                onEvent(CategoryDetailUiEvent.OnDeleteMediaClicked(item))
                            }
                        )
                    }
                }

                activeMediaItems.isEmpty() -> {
                    EmptyMediaTabState(selectedTab = activeMediaTab)
                }

                else -> {
                    MediaSquareGrid(
                        items = activeMediaItems,
                        selectedTab = activeMediaTab,
                        onItemClick = { item, index ->
                            when (activeMediaTab) {
                                MediaType.IMAGE -> onEvent(
                                    CategoryDetailUiEvent.OnImageClicked(item.id, index)
                                )
                                MediaType.VIDEO -> onEvent(
                                    CategoryDetailUiEvent.OnVideoClicked(item.id, index)
                                )
                                MediaType.DOCUMENT -> Unit
                            }
                        },
                        onItemLongClick = { item ->
                            onEvent(CategoryDetailUiEvent.OnDeleteMediaClicked(item))
                        }
                    )
                }
            }
        }
    }

    if (uiState.isBrowseSheetVisible) {
        BrowseStorageBottomSheet(
            categoryType = uiState.categoryType,
            onSelectFiles = { onEvent(CategoryDetailUiEvent.OnSelectFilesClicked) },
            onSelectFolder = { onEvent(CategoryDetailUiEvent.OnSelectFolderClicked) },
            onDismiss = { onEvent(CategoryDetailUiEvent.OnDismissBrowseSheet) }
        )
    }

    uiState.deleteConfirmationItem?.let { item ->
        AlertDialog(
            onDismissRequest = { onEvent(CategoryDetailUiEvent.OnDismissDeleteMediaDialog) },
            title = {
                Text(text = stringResource(R.string.media_delete_dialog_title, item.displayName))
            },
            text = {
                Text(text = stringResource(R.string.media_delete_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { onEvent(CategoryDetailUiEvent.OnConfirmDeleteMedia) }) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CategoryDetailUiEvent.OnDismissDeleteMediaDialog) }) {
                    Text(text = stringResource(R.string.category_dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun MediaSquareGrid(
    items: List<MediaItem>,
    selectedTab: MediaType,
    onItemClick: (MediaItem, Int) -> Unit,
    onItemLongClick: (MediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 4.dp,
            end = 4.dp,
            top = 4.dp,
            bottom = 88.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id },
            contentType = { _, _ -> selectedTab }
        ) { index, item ->
            MediaSquareGridItem(
                item = item,
                onClick = { onItemClick(item, index) },
                onLongClick = { onItemLongClick(item) }
            )
        }
    }
}

@Composable
private fun DocumentList(
    items: List<MediaItem>,
    onDocumentClick: (MediaItem) -> Unit,
    onDocumentLongClick: (MediaItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { item -> item.id },
            contentType = { "document_list_item" }
        ) { item ->
            DocumentListItem(
                item = item,
                onClick = { onDocumentClick(item) },
                onLongClick = { onDocumentLongClick(item) }
            )
        }
    }
}

@Composable
private fun EmptyMediaTabState(selectedTab: MediaType) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (selectedTab) {
                    MediaType.IMAGE -> Icons.Outlined.PhotoLibrary
                    MediaType.VIDEO -> Icons.Outlined.VideoLibrary
                    MediaType.DOCUMENT -> Icons.Outlined.Description
                },
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    when (selectedTab) {
                        MediaType.IMAGE -> R.string.category_detail_empty_images
                        MediaType.VIDEO -> R.string.category_detail_empty_videos
                        MediaType.DOCUMENT -> R.string.category_detail_empty_documents
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
