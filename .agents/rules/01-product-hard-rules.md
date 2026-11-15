# Product & Business Hard Rules — MediaArchive

This document defines the strict product and business constraints for **MediaArchive**. Every screen, UseCase, and test must comply with these rules.

---

## HR-01: Strict Manual Media Selection Only (No Auto-Scan)

- **Required**:
  - On startup, the app reads only the records already saved in SQLite (`categories` and `media_items`).
  - Accessing device or external storage happens **only** when the user taps **"Browse Storage"** inside a specific Category.
- **Forbidden**:
  - Never query `MediaStore.Video` or `MediaStore.Images` automatically to aggregate all media on the device.
  - Never run background workers or services that scan directories without explicit user selection.

---

## HR-02: Default `Main` Category & Mandatory Category Selection

- **Required**:
  - Upon initial database creation, the system must seed the default Category:
    - `name`: `"Main"`
    - `accessMode`: `PUBLIC`
    - `isDefault`: `true`
  - Users must select a Category from the main screen before viewing or adding any images/videos.
- **Forbidden**:
  - Do not allow deleting the default `Main` Category or changing its `isDefault` status.
  - Never insert `media_items` without a valid `category_id` (`NOT NULL` foreign key).

---

## HR-03: Category Access Modes (`public` vs `private`) & Unrestricted Passwords

- **Creating a Category**:
  - The user enters a unique Category name and selects either `public` or `private` mode.
  - If `private` is selected, a password input field is required.
- **Password Rules for `private` Categories**:
  - **No minimum length or character restrictions**: A 1-character password (e.g., `"a"`), a 1-digit password (e.g., `"1"`), or any non-empty string (`length >= 1`) is **VALID**.
  - Reject the password only when it is completely empty (`isEmpty()`).
- **Accessing a Category**:
  - If `mode == PUBLIC`: Navigate directly to `CategoryDetailScreen`.
  - If `mode == PRIVATE`: Always prompt for the password when selected from the Category list. Navigate to `CategoryDetailScreen` only after password verification succeeds.

---

## HR-04: Category Detail Layout — 2 Separate Tabs & 1:1 Square Grid

- **Required**:
  - The Category detail screen must feature **2 distinct tabs**:
    1. **Images Tab**
    2. **Videos Tab**
  - Each tab displays only its respective media type in a **Square Grid (`aspectRatio(1f)`)**.
  - Grid cells load the cached thumbnail from `thumbnail_path`. Video grid cells display a subtle play icon and duration badge.

---

## HR-05: Full-Screen Image Viewer & Video Player Controls

- **Image Viewer**:
  - Tapping an image cell opens a **full-screen viewer** starting at the tapped image.
  - Users **swipe left / right** (`HorizontalPager`) to view the next or previous image in the same Category.
- **Video Player**:
  - Tapping a video cell opens the video player for that video.
  - The **Player Controller Bar** must include **Previous** and **Next** buttons to switch directly to the previous or next video in the same Category.

---

## HR-06: Storage Browsing (Single Files & Folders) + SQLite Persistence

- Inside a Category, tapping **"Browse Storage"** offers two options:
  1. **Select Images / Videos** (one or multiple individual files).
  2. **Select Folder** from Local or External Storage.
- **When individual files are selected**:
  - Take persistable read permission (`takePersistableUriPermission`).
  - Extract and save a thumbnail file to app-private storage.
  - Save `content_uri` + `thumbnail_path` + `media_type` + `category_id` into SQLite.
- **When a folder is selected**:
  - Take persistable read permission for the folder tree.
  - Traverse all image and video files inside the selected folder.
  - For each media file found: extract and save its thumbnail, and insert `content_uri` + `thumbnail_path` into SQLite under the active `category_id`.

---

## HR-07: Themes, Localization (`en`, `vi`, `ja`), `Settings` & Concise `About` Screen

- **Multi-Theme System**:
  - **Default Theme (`Neutral Taupe`)**: Neutral palette combining White, Black, and Taupe / Brownish Gray.
  - **Additional Deep Accent Themes**:
    - `Deep Crimson` (dark crimson red tones)
    - `Deep Forest` (deep forest/emerald green tones)
    - `Deep Sapphire` (deep navy/ocean blue tones)
  - Both Light and Dark appearance modes (or deep-toned surfaces) must remain legible and harmonious.
- **Settings Screen**:
  - Users change the **Theme Palette** and **App Language** from the **Settings** screen.
- **3 Supported Languages**:
  - Full string resource localization for **English (`en`)**, **Vietnamese (`vi`)**, and **Japanese (`ja`)**.
- **Concise `About` Screen**:
  - The `About` screen must **only briefly describe the app's purpose and user-facing features** (e.g., organizing and viewing personal videos and photos by public/private categories).
  - **STRICTLY FORBIDDEN on the `About` screen**: Do not mention or advertise internal software architecture, coding patterns, or tech stack libraries (no mention of Clean Architecture, MVVM, Kotlin, Jetpack Compose, Room, SQLite, SAF, ExoPlayer, etc.).
