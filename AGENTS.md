# MediaArchive — Project Rules & Agent Instructions

This document defines the non-negotiable **Hard Rules** and architectural guidelines that every AI Agent and Developer must follow when working on the **MediaArchive** Android project.

---

## 1. Project Overview

**MediaArchive** is an Android application for organizing and viewing Images and Videos by user-defined **Categories** selected manually from device storage, without automatically scanning the entire device.

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVVM (Unidirectional Data Flow)
- **Database**: SQLite via AndroidX Room
- **Preferences**: Jetpack DataStore (Preferences) for Theme & Language settings
- **Storage Access**: Storage Access Framework (SAF — `OpenMultipleDocuments` & `OpenDocumentTree`)
- **Image Viewer / Video Player**: Compose `HorizontalPager` + AndroidX Media3 (`ExoPlayer`)
- **Supported App Languages**: English (`en`), Vietnamese (`vi`), Japanese (`ja`)

---

## 2. Non-Negotiable Hard Rules

Every line of code in this repository **MUST** comply with the rules below. Do not omit or alter these behaviors without an explicit request from the User:

1. **[HR-01] No Auto-Scan Storage**:
   - The app **MUST NOT** automatically scan `MediaStore` or traverse device directories in the background on startup.
   - Images and videos are added **only** when the user explicitly taps the **"Browse Storage"** button inside a Category and selects individual files or a folder.
2. **[HR-02] Mandatory Category Selection & Default `Main` Categories**:
   - Users **must select a Category first** before browsing or viewing images/videos/documents.
   - The system always initializes two default Categories named `Main` with `public` access mode (`is_default = true`): one for **Media** (`CategoryType.MEDIA`) and one for **Documents** (`CategoryType.DOCUMENT`). Default `Main` Categories cannot be deleted.
3. **[HR-03] Category Type (`MEDIA` / `DOCUMENT`), `public` / `private` Modes & Unrestricted Password Policy**:
   - User-created Categories allow setting a custom name (unique per `category_type`), choosing the Category type (**Media** or **Document**), and choosing either `public` or `private` mode:
     - **`public` mode**: Tapping the Category opens its content immediately.
     - **`private` mode**:
       - Creating a `private` Category requires entering a password.
       - **Unrestricted password format**: Any non-empty password is valid (a single character, a single digit, spaces, etc. are all accepted). **DO NOT** enforce minimum length (such as `>= 6 chars`) or complexity rules.
       - Tapping a `private` Category from the Category list always requires entering the matching password before granting access.
4. **[HR-04] Dedicated Category Detail Views & Uniform Dimensions**:
   - **Media Category (`CategoryType.MEDIA`)**: Features **2 separate tabs** (**Images** and **Videos**), each rendered in a **1:1 Square Grid**.
   - **Document Category (`CategoryType.DOCUMENT`)**: Renders directly as a **vertical list** (no tab bar) where every item has an **identical fixed height (`72.dp`)**, clearly distinguishes file extensions (`.txt`, `.pdf`, `.docx`, `.xlsx`, `.pptx`, etc.) via color-coded extension badges, and truncates long filenames to a single line with ellipsis (`...`).
   - **Category List Uniformity**: On `CategoryListScreen`, the default `Main` cards and all user-created `public`/`private` Category cards **must have identical height (`72.dp`)** with a clean 4-column layout (private lock slot | type icon | name | delete action slot).
5. **[HR-05] Image Viewing, Video Playback & External Document Opening**:
   - **Image Viewer**: Tapping an image opens it in **full-screen mode**, allowing navigation to the previous or next image in the Category via **horizontal swipe gestures (left/right)**.
   - **Video Player**: Tapping a video opens the video player, allowing navigation to the previous or next video in the Category by **tapping the Previous / Next buttons on the controller bar**.
   - **Document Opener**: Tapping a document opens it via an installed external Android app (`Intent.ACTION_VIEW` with `FLAG_GRANT_READ_URI_PERMISSION`), showing a graceful Snackbar if no compatible app is installed.
6. **[HR-06] Manual Storage Browsing (Type-Filtered Files & Recursive Folders), Hidden/Cache Filtering, Canonical Deduplication & SQLite Persistence**:
   - Tapping **"Browse Storage"** lets the user pick **individual files** matching the Category's type (`image/*`, `video/*` for Media; Text, PDF, Word, Excel, PowerPoint, OpenDocument for Document) or a **folder** from Local / External Storage:
     - **Individual files selected**: Validate against the Category's `CategoryType`, generate thumbnails for images/videos (skip for documents), save thumbnail files to app-private storage, and store `content_uri` + `canonical_id` + `thumbnail_path` in SQLite.
     - **Folder selected**: Recursively traverse the folder and its nested subfolders (skipping hidden/cache folders such as `.thumbnails`, `thumbnails`, `.trash`, `.cache`, and dot-prefixed hidden files), filter to only files matching the Category's `CategoryType`, and flatten them directly into SQLite without rendering subfolder UI.
     - **Canonical Deduplication**: Prevent duplicate imports within a Category using canonical SAF document identity (`authority::documentId`), clean up orphan thumbnails on conflict, and report skipped duplicates to the user.
7. **[HR-07] UI Design, Multi-Theme, 3 Languages (`en`, `vi`, `ja`) & Concise `About` Screen**:
   - **Default Neutral Theme**: Uses a clean neutral palette of **White, Black, and Taupe (Brownish Gray)**.
   - **Additional Rich/Deep Themes**: Provide deep, tasteful accent palettes including **Deep Crimson Red**, **Deep Forest Green**, and **Deep Navy/Sapphire Blue**.
   - **Settings Screen**: Users can switch both the **App Theme/Palette** and **App Language** inside the **Settings** screen.
   - **3 Supported Languages**: Full UI localization in **English (`en`)**, **Vietnamese (`vi`)**, and **Japanese (`ja`)**.
   - **`About` Screen Content**: The `About` screen **only briefly introduces the app's user-facing features**. **NEVER** advertise or mention technical architecture, frameworks, or libraries (e.g., do not mention Clean Architecture, MVVM, Jetpack Compose, Room, SQLite, SAF, or ExoPlayer on the `About` screen).
8. **[HR-08] Source Code Security & Documentation Language**:
   - `private` Category passwords must be hashed (`PBKDF2WithHmacSHA256` or `SHA-256` + random per-category salt); never store plain-text passwords.
   - Thumbnails must be stored in `context.filesDir/thumbnails/` with a `.nomedia` file and UUID filenames.
   - Enable `FLAG_SECURE` when viewing `private` Categories.
   - **All project documentation and `.md` files must be written in English.**

---

## 3. Project Documentation Index (`docs/`)

Before starting any implementation phase, read the relevant documents below:

- **Detailed Rules**:
  - [01-product-hard-rules.md](.agents/rules/01-product-hard-rules.md)
  - [02-android-architecture-rules.md](.agents/rules/02-android-architecture-rules.md)
- **System Architecture (`docs/architecture/`)**:
  - [01-system-architecture.md](docs/architecture/01-system-architecture.md)
  - [02-database-schema.md](docs/architecture/02-database-schema.md)
  - [03-media-ingestion-and-playback.md](docs/architecture/03-media-ingestion-and-playback.md)
  - [04-design-system-and-localization.md](docs/architecture/04-design-system-and-localization.md)
- **Architecture Decision Records (`docs/decisions/`)**:
  - [ADR-001: Manual Storage Selection via SAF](docs/decisions/ADR-001-manual-storage-selection-saf.md)
  - [ADR-002: SQLite (Room) Schema & Thumbnail Storage](docs/decisions/ADR-002-sqlite-room-and-thumbnail-storage.md)
  - [ADR-003: Category Public/Private Access & Unrestricted Password](docs/decisions/ADR-003-category-privacy-and-password.md)
  - [ADR-004: Full-screen Image Pager & Media3 Video Player](docs/decisions/ADR-004-image-pager-and-media3-video-player.md)
  - [ADR-005: Multi-Theme Palettes, Localization (`en`/`vi`/`ja`) & Concise About Screen](docs/decisions/ADR-005-theming-localization-and-about-screen.md)
  - [ADR-006: Office Documents Tab, Explorer-Style Nested Folders, Category Card Uniformity & Canonical SAF Deduplication](docs/decisions/ADR-006-documents-folder-explorer-and-deduplication.md)
- **Naming & Coding Conventions (`docs/standards/`)**:
  - [kotlin-android-naming-conventions.md](docs/standards/kotlin-android-naming-conventions.md)
- **GitHub Workflow (`docs/github/`)**:
  - [github-workflow-guide.md](docs/github/github-workflow-guide.md)
- **Implementation Roadmap (`docs/roadmap/`)**:
  - [implementation-phases.md](docs/roadmap/implementation-phases.md)
