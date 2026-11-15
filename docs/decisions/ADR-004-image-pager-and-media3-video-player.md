# ADR-004: Full-Screen Image Pager (`HorizontalPager`) & `Media3 ExoPlayer` Video Player

- **Status**: Approved
- **Date**: 2026-11-15
- **Related Rules**: HR-04, HR-05

---

## 1. Context

Inside a Category, images and videos are separated into 2 tabs rendered as a 1:1 Square Grid:
- **Tapping an Image**: Opens the image in full-screen mode and allows switching to the previous or next image by **swiping the screen horizontally**.
- **Tapping a Video**: Opens the video player and allows switching to the previous or next video by **tapping the Previous / Next buttons on the controller bar**.

---

## 2. Decision

1. **Full-Screen Image Viewer (`ImageViewerScreen`)**:
   - Built with Jetpack Compose's `HorizontalPager` (`androidx.compose.foundation.pager.HorizontalPager`).
   - Initialized at the tapped image's index (`rememberPagerState(initialPage = initialIndex) { images.size }`).
   - Users swipe left or right to transition smoothly between images in the same Category.
2. **Video Player (`VideoPlayerScreen`)**:
   - Built with **AndroidX Media3 (`ExoPlayer`)**.
   - Loads all videos in the active Category into the player's playlist (`setMediaItems(mediaItems, initialIndex, C.TIME_UNSET)`).
   - Provides explicit **Previous (`seekToPreviousMediaItem()`)** and **Next (`seekToNextMediaItem()`)** buttons on the player controller bar, avoiding gesture conflicts with the video seek bar.

---

## 3. Consequences

### Positive:
- Native playlist support in `ExoPlayer` enables instant transitions between previous and next videos using a single player instance.
- Clear UX distinction: swipe gestures for full-screen photos, dedicated controller buttons for video playback.
