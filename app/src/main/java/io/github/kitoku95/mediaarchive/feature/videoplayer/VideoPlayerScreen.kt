package io.github.kitoku95.mediaarchive.feature.videoplayer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.feature.videoplayer.component.VideoPlayerControlsOverlay
import kotlinx.coroutines.delay

/**
 * Stateful route connecting [VideoPlayerViewModel] to [VideoPlayerScreen].
 */
@Composable
fun VideoPlayerRoute(
    onNavigateBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                VideoPlayerUiEffect.NavigateBack -> onNavigateBack()
                is VideoPlayerUiEffect.ShowFileMissingMessage -> {
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
                is VideoPlayerUiEffect.ShowPlaybackError -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.video_player_playback_error, effect.displayName)
                    )
                }
            }
        }
    }

    VideoPlayerScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun VideoPlayerScreen(
    uiState: VideoPlayerUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (VideoPlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPlaylistInitialized by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var totalCount by remember { mutableIntStateOf(0) }
    var hasPrevious by remember { mutableStateOf(false) }
    var hasNext by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(view, isControlsVisible) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (isControlsVisible) {
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

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                isPlaying = player.isPlaying
                currentIndex = player.currentMediaItemIndex.coerceAtLeast(0)
                totalCount = player.mediaItemCount
                hasPrevious = player.hasPreviousMediaItem()
                hasNext = player.hasNextMediaItem()
                val rawDuration = player.duration
                if (rawDuration != C.TIME_UNSET && rawDuration >= 0L) {
                    durationMs = rawDuration
                }
                if (!isScrubbing) {
                    currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val failingIndex = exoPlayer.currentMediaItemIndex
                val failingMediaId = exoPlayer.currentMediaItem?.mediaId?.toLongOrNull()
                if (failingMediaId != null) {
                    onEvent(VideoPlayerUiEvent.OnMediaFileMissing(failingMediaId))
                }
                if (failingIndex in 0 until exoPlayer.mediaItemCount) {
                    exoPlayer.removeMediaItem(failingIndex)
                    if (exoPlayer.mediaItemCount > 0) {
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.videos) {
        if (!isPlaylistInitialized && uiState.videos.isNotEmpty()) {
            val media3Items = uiState.videos.map { video ->
                androidx.media3.common.MediaItem.Builder()
                    .setMediaId(video.id.toString())
                    .setUri(Uri.parse(video.contentUri))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(video.displayName)
                            .build()
                    )
                    .build()
            }
            val startIndex = uiState.initialIndex.coerceIn(0, media3Items.lastIndex)
            exoPlayer.setMediaItems(media3Items, startIndex, C.TIME_UNSET)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            isPlaylistInitialized = true
            currentIndex = startIndex
            totalCount = media3Items.size
        }
    }

    LaunchedEffect(isPlaying, isScrubbing, exoPlayer) {
        while (isPlaying && !isScrubbing) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            val rawDuration = exoPlayer.duration
            if (rawDuration != C.TIME_UNSET && rawDuration >= 0L) {
                durationMs = rawDuration
            }
            delay(250L)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isControlsVisible = !isControlsVisible
                }
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                uiState.videos.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.video_player_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                player = exoPlayer
                            }
                        },
                        update = { playerView ->
                            if (playerView.player !== exoPlayer) {
                                playerView.player = exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    val activeTitle = exoPlayer.currentMediaItem
                        ?.mediaMetadata
                        ?.title
                        ?.toString()
                        ?: uiState.videos.getOrNull(currentIndex)?.displayName.orEmpty()

                    VideoPlayerControlsOverlay(
                        visible = isControlsVisible,
                        title = activeTitle,
                        currentIndex = currentIndex,
                        totalCount = if (totalCount > 0) totalCount else uiState.videos.size,
                        isPlaying = isPlaying,
                        hasPrevious = hasPrevious,
                        hasNext = hasNext,
                        positionMs = currentPositionMs,
                        durationMs = durationMs,
                        onBackClick = { onEvent(VideoPlayerUiEvent.OnBackClicked) },
                        onPreviousClick = {
                            if (exoPlayer.hasPreviousMediaItem()) {
                                exoPlayer.seekToPreviousMediaItem()
                            }
                        },
                        onPlayPauseClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        onNextClick = {
                            if (exoPlayer.hasNextMediaItem()) {
                                exoPlayer.seekToNextMediaItem()
                            }
                        },
                        onSeekStart = { newPos ->
                            isScrubbing = true
                            currentPositionMs = newPos
                        },
                        onSeekFinish = { finalPos ->
                            exoPlayer.seekTo(finalPos)
                            isScrubbing = false
                        }
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
