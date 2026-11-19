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

## HR-04: Category Detail Layout — 3 Separate Tabs, Explorer Folders & Uniform Dimensions

- **Required**:
  - The Category detail screen must feature **3 distinct tabs**:
    1. **Images Tab** — 1:1 Square Grid (`aspectRatio(1f)`) showing child folders (uniform square tiles) and `IMAGE` thumbnails.
    2. **Videos Tab** — 1:1 Square Grid (`aspectRatio(1f)`) showing child folders (uniform square tiles) and `VIDEO` thumbnails with play icon and duration badge.
    3. **Documents Tab** — Vertical list (`LazyColumn`) showing child folders and `DOCUMENT` items (`.txt`, `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`, etc.) where:
       - Every list row has an **identical fixed height (`72.dp`)**.
       - File extensions are clearly distinguished via color-coded extension badges.
       - Long filenames are truncated to a single line with ellipsis (`...`).
  - **Explorer-Style Subfolder Navigation**:
    - Tapping a folder opens its contents (child subfolders and files) with a breadcrumb bar and Back navigation to step up levels.
  - **Category List Card Uniformity**:
    - On `CategoryListScreen`, the default `Main` card and all user-created `public`/`private` cards must share the exact same fixed height (`72.dp`).

---

## HR-05: Full-Screen Image Viewer, Video Player Controls & External Document Opening

- **Image Viewer**:
  - Tapping an image cell opens a **full-screen viewer** starting at the tapped image.
  - Users **swipe left / right** (`HorizontalPager`) to view the next or previous image in the active folder / Category.
- **Video Player**:
  - Tapping a video cell opens the video player for that video.
  - The **Player Controller Bar** must include **Previous** and **Next** buttons to switch directly to the previous or next video in the active folder / Category.
- **Document Opener**:
  - Tapping a document launches an installed external Android app via `Intent.ACTION_VIEW` with `Intent.FLAG_GRANT_READ_URI_PERMISSION`.
  - If no app on the device can handle the file type, show a localized Snackbar notification without crashing.

---

## HR-06: Storage Browsing (Files & Nested Folders), Canonical Deduplication & SQLite Persistence

- Inside a Category (at root or inside any opened subfolder), tapping **"Browse Storage"** offers two options:
  1. **Select Files** (one or multiple individual images, videos, or documents: `.txt`, `.md`, `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.csv`, `.ppt`, `.pptx`, `.rtf`, `.odt`, `.ods`, `.odp`).
  2. **Select Folder** from Local or External Storage.
- **When individual files are selected**:
  - Take persistable read permission (`takePersistableUriPermission`).
  - Extract and save a `.webp` thumbnail for `IMAGE`/`VIDEO` (skip thumbnail extraction for `DOCUMENT`, storing `thumbnail_path = ""`).
  - Save `content_uri` + `canonical_id` + `thumbnail_path` + `media_type` + `category_id` + `folder_id` into SQLite.
- **When a folder is selected**:
  - Take persistable read permission for the folder tree.
  - Create the selected folder itself as an `ArchiveFolder` item at the current folder level (`parent_folder_id = currentFolderId`).
  - Recursively preserve all nested subfolders (`archive_folders`) and discovered images, videos, and documents (`media_items`) in SQLite.
- **Canonical Deduplication**:
  - Enforce `UNIQUE(category_id, canonical_id)` where `canonical_id` normalizes SAF URIs (`authority::documentId`) so selecting the same physical file individually and via folder import never creates duplicate entries.
  - Clean up any newly generated thumbnail if an insert conflict occurs, and inform the user when duplicates are skipped.

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
