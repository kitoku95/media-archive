package io.github.kitoku95.mediaarchive.feature.imageviewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import kotlinx.coroutines.coroutineScope
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.MediaItem

/**
 * Stateful route connecting [ImageViewerViewModel] to [ImageViewerScreen] and system bar visibility.
 */
@Composable
fun ImageViewerRoute(
    onNavigateBack: () -> Unit,
    viewModel: ImageViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(view, uiState.isOverlayVisible) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (uiState.isOverlayVisible) {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {}
    }

    DisposableEffect(view) {
        onDispose {
            val window = context.findActivity()?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ImageViewerUiEffect.NavigateBack -> onNavigateBack()
                is ImageViewerUiEffect.ShowFileMissingMessage -> {
                    val message = context.getString(
                        R.string.media_file_missing_removed,
                        effect.displayName
                    )
                    if (effect.shouldNavigateBack) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    } else {
                        snackbarHostState.showSnackbar(message)
                    }
                }
                is ImageViewerUiEffect.ShowDeleteSuccess -> {
                    val message = context.getString(
                        R.string.media_delete_success,
                        effect.displayName
                    )
                    if (effect.shouldNavigateBack) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        snackbarHostState.showSnackbar(message)
                    }
                }
                ImageViewerUiEffect.ShowDeleteError -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.media_delete_error)
                    )
                }
            }
        }
    }

    ImageViewerScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun ImageViewerScreen(
    uiState: ImageViewerUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ImageViewerUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                uiState.images.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.image_viewer_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    val safeInitialIndex = uiState.initialIndex.coerceIn(0, uiState.images.lastIndex)
                    val pagerState = rememberPagerState(
                        initialPage = safeInitialIndex,
                        pageCount = { uiState.images.size }
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val item = uiState.images[page]
                        ZoomableImagePage(
                            item = item,
                            isCurrentPage = pagerState.currentPage == page,
                            onTap = { onEvent(ImageViewerUiEvent.OnToggleOverlay) },
                            onMissingFile = {
                                onEvent(ImageViewerUiEvent.OnMediaFileMissing(item))
                            }
                        )
                    }

                    val currentItem = uiState.images.getOrNull(pagerState.currentPage)
                    AnimatedVisibility(
                        visible = uiState.isOverlayVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        ImageViewerTopBar(
                            title = currentItem?.displayName.orEmpty(),
                            currentIndex = pagerState.currentPage + 1,
                            totalCount = uiState.images.size,
                            onBackClick = { onEvent(ImageViewerUiEvent.OnBackClicked) },
                            onDeleteClick = currentItem?.let { item ->
                                { onEvent(ImageViewerUiEvent.OnDeleteClicked(item)) }
                            }
                        )
                    }
                }
            }
        }
    }

    uiState.deleteConfirmationItem?.let { item ->
        AlertDialog(
            onDismissRequest = { onEvent(ImageViewerUiEvent.OnDismissDeleteDialog) },
            title = {
                Text(text = stringResource(R.string.media_delete_dialog_title, item.displayName))
            },
            text = {
                Text(text = stringResource(R.string.media_delete_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { onEvent(ImageViewerUiEvent.OnConfirmDelete) }) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ImageViewerUiEvent.OnDismissDeleteDialog) }) {
                    Text(text = stringResource(R.string.category_dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun ZoomableImagePage(
    item: MediaItem,
    isCurrentPage: Boolean,
    onTap: () -> Unit,
    onMissingFile: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage && scale != 1f) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(item.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(item.id) {
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val pointerCount = event.changes.count { it.pressed }
                            if (pointerCount > 1 || scale > 1f) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                                val maxOffsetX =
                                    ((containerSize.width * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                                val maxOffsetY =
                                    ((containerSize.height * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                                scale = newScale
                                offset = if (newScale > 1f) {
                                    Offset(
                                        x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    Offset.Zero
                                }
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Uri.parse(item.contentUri))
                .placeholderMemoryCacheKey(item.thumbnailPath)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            onError = { onMissingFile() },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@Composable
private fun ImageViewerTopBar(
    title: String,
    currentIndex: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.72f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                tint = Color.White
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.viewer_index_counter, currentIndex, totalCount),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(end = 4.dp)
        )
        if (onDeleteClick != null) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.media_delete_action),
                    tint = Color.White
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
