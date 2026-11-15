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
2. **[HR-02] Mandatory Category Selection & Default `Main` Category**:
   - Users **must select a Category first** before browsing or viewing images/videos.
   - The system always initializes a default Category named `Main` with `public` access mode (`is_default = true`). The `Main` Category cannot be deleted.
3. **[HR-03] `public` / `private` Modes & Unrestricted Password Policy**:
   - User-created Categories allow setting a custom name and choosing either `public` or `private` mode:
     - **`public` mode**: Tapping the Category opens its media content immediately.
     - **`private` mode**:
       - Creating a `private` Category requires entering a password.
       - **Unrestricted password format**: Any non-empty password is valid (a single character, a single digit, spaces, etc. are all accepted). **DO NOT** enforce minimum length (such as `>= 6 chars`) or complexity rules.
       - Tapping a `private` Category from the Category list always requires entering the matching password before granting access.
4. **[HR-04] Category Detail View: 2 Separate Tabs & Square Grid**:
   - Inside a Category, the screen has **2 separate tabs**: **Images** tab and **Videos** tab.
   - Selecting a tab displays only that media type in a **1:1 Square Grid**.
5. **[HR-05] Image Viewing & Video Playback Experience**:
   - **Image Viewer**: Tapping an image opens it in **full-screen mode**, allowing navigation to the previous or next image in the same Category via **horizontal swipe gestures (left/right)**.
   - **Video Player**: Tapping a video opens the video player, allowing navigation to the previous or next video in the same Category by **tapping the Previous / Next buttons on the controller bar**.
6. **[HR-06] Manual Storage Browsing (Files & Folders) + SQLite Persistence**:
   - Tapping **"Browse Storage"** lets the user pick **individual images/videos** or a **folder** from Local / External Storage:
     - **Individual files selected**: Generate thumbnails, save thumbnail files to app-private storage, and store `content_uri` + `thumbnail_path` in SQLite.
     - **Folder selected**: Traverse all images/videos inside that folder, generate thumbnails, and store `content_uri` + `thumbnail_path` for every discovered media file in SQLite.
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
- **Naming & Coding Conventions (`docs/standards/`)**:
  - [kotlin-android-naming-conventions.md](docs/standards/kotlin-android-naming-conventions.md)
- **GitHub Workflow (`docs/github/`)**:
  - [github-workflow-guide.md](docs/github/github-workflow-guide.md)
- **Implementation Roadmap (`docs/roadmap/`)**:
  - [implementation-phases.md](docs/roadmap/implementation-phases.md)
