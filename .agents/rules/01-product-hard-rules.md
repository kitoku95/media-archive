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

## HR-02: Default `Main` Categories & Mandatory Category Selection

- **Required**:
  - Upon initial database creation, the system must seed two default Categories:
    - Media default: `name = "Main"`, `categoryType = MEDIA`, `accessMode = PUBLIC`, `isDefault = true`
    - Document default: `name = "Main"`, `categoryType = DOCUMENT`, `accessMode = PUBLIC`, `isDefault = true`
  - Users must select a Category from the main screen before viewing or adding any images/videos/documents.
- **Forbidden**:
  - Do not allow deleting the default `Main` Categories or changing their `isDefault` status.
  - Never insert `media_items` without a valid `category_id` (`NOT NULL` foreign key).

---

## HR-03: Category Type (`MEDIA` vs `DOCUMENT`), Access Modes (`public` vs `private`) & Unrestricted Passwords

- **Creating a Category**:
  - The user enters a Category name (unique per `category_type`), chooses the Category type (`MEDIA` or `DOCUMENT`), and selects either `public` or `private` mode.
  - If `private` is selected, a password input field is required.
- **Password Rules for `private` Categories**:
  - **No minimum length or character restrictions**: A 1-character password (e.g., `"a"`), a 1-digit password (e.g., `"1"`), or any non-empty string (`length >= 1`) is **VALID**.
  - Reject the password only when it is completely empty (`isEmpty()`).
- **Accessing a Category**:
  - If `mode == PUBLIC`: Navigate directly to `CategoryDetailScreen`.
  - If `mode == PRIVATE`: Always prompt for the password when selected from the Category list. Navigate to `CategoryDetailScreen` only after password verification succeeds.

---

## HR-04: Dedicated Category Detail Layouts & Uniform Dimensions

- **Required**:
  - **Media Category (`CategoryType.MEDIA`)**:
    - Displays **2 distinct tabs**:
      1. **Images Tab** — 1:1 Square Grid (`aspectRatio(1f)`) showing `IMAGE` thumbnails.
      2. **Videos Tab** — 1:1 Square Grid (`aspectRatio(1f)`) showing `VIDEO` thumbnails with play icon and duration badge.
  - **Document Category (`CategoryType.DOCUMENT`)**:
    - Displays directly as a **vertical list (`LazyColumn`)** with no tab bar, showing `DOCUMENT` items (`.txt`, `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`, etc.) where:
      - Every list row has an **identical fixed height (`72.dp`)**.
      - File extensions are clearly distinguished via color-coded extension badges.
      - Long filenames are truncated to a single line with ellipsis (`...`).
  - **Category List Card Uniformity**:
    - On `CategoryListScreen`, the default `Main` cards and all user-created `public`/`private` cards must share the exact same fixed height (`72.dp`) using a 4-column layout:
      1. Private lock icon (hidden when public)
      2. Category type icon (Folder for Media, Document for Document)
      3. Category name (widest column)
      4. Delete action button (hidden for default `Main` categories)

---

## HR-05: Full-Screen Image Viewer, Video Player Controls & External Document Opening

- **Image Viewer**:
  - Tapping an image cell opens a **full-screen viewer** starting at the tapped image.
  - Users **swipe left / right** (`HorizontalPager`) to view the next or previous image in the Category.
- **Video Player**:
  - Tapping a video cell opens the video player for that video.
  - The **Player Controller Bar** must include **Previous** and **Next** buttons to switch directly to the previous or next video in the Category.
- **Document Opener**:
  - Tapping a document launches an installed external Android app via `Intent.ACTION_VIEW` with `Intent.FLAG_GRANT_READ_URI_PERMISSION`.
  - If no app on the device can handle the file type, show a localized Snackbar notification without crashing.

---

## HR-06: Type-Filtered Storage Browsing (Files & Recursive Folders), Canonical Deduplication & SQLite Persistence

- Inside a Category, tapping **"Browse Storage"** offers two options:
  1. **Select Files** filtered to the Category's type (`image/*`, `video/*` for Media; `.txt`, `.md`, `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.csv`, `.ppt`, `.pptx`, `.rtf`, `.json`, `.xml`, `.odt`, `.ods`, `.odp` for Document).
  2. **Select Folder** from Local or External Storage.
- **When individual files are selected**:
  - Take persistable read permission (`takePersistableUriPermission`).
  - Keep only files matching the Category's `CategoryType`.
  - Extract and save a `.webp` thumbnail for `IMAGE`/`VIDEO` (skip thumbnail extraction for `DOCUMENT`, storing `thumbnail_path = ""`).
  - Save `content_uri` + `canonical_id` + `thumbnail_path` + `media_type` + `category_id` into SQLite.
- **When a folder is selected**:
  - Take persistable read permission for the folder tree.
  - Recursively traverse the folder and its nested subfolders, skipping hidden/cache directories (`.thumbnails`, `thumbnails`, `.trash`, `.cache`, dot-prefixed folders/files), keeping only files matching the Category's `CategoryType`, and flattening them directly into SQLite.
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
