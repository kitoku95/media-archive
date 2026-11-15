# MediaArchive (Android)

An Android application for organizing and viewing **Images and Videos** by **Category** (`public` / `private`) through user-initiated storage selection (**Storage Access Framework**), without automatically scanning device storage.

---

## Core Features

- **No Auto-Scan Storage**: Media files are added only when the user explicitly taps **"Browse Storage"** to select individual files or an entire folder.
- **Category Organization (`public` & `private`)**:
  - Mandatory Category selection before browsing media; ships with a built-in default `Main` (`public`) Category.
  - Custom Categories support `public` mode (instant access) or `private` mode (password-protected on creation and access, accepting any non-empty password without length or character restrictions).
- **Dual Tabs & Square Grid Layout**:
  - Separate **Images** and **Videos** tabs inside each Category, rendered in a 1:1 square grid.
- **Media Viewing & Playback**:
  - **Image Viewer**: Full-screen view with horizontal swipe gestures to navigate to the previous or next image.
  - **Video Player**: Full-screen player (`Media3 ExoPlayer`) with **Previous / Next** buttons on the controller bar.
- **SQLite & Thumbnail Persistence**:
  - Extracts thumbnails and persists both `content_uri` and `thumbnail_path` in SQLite (`Room`) for single files and folders.
- **Multi-Theme & Multi-Language (`en`, `vi`, `ja`)**:
  - Default neutral theme (**White, Black, Taupe / Brownish Gray**) plus deep accent themes (**Deep Crimson**, **Deep Forest Green**, **Deep Ocean Blue**) configurable in **Settings**.
  - Supports **English**, **Vietnamese**, and **Japanese** selectable in **Settings**.
  - Minimalist **About** screen describing only user-facing functionality without technical jargon.

---

## Project Documentation (`docs/`)

- **Project Rules & Hard Rules**:
  - [AGENTS.md](AGENTS.md)
  - [01-product-hard-rules.md](.agents/rules/01-product-hard-rules.md)
  - [02-android-architecture-rules.md](.agents/rules/02-android-architecture-rules.md)
- **System Architecture (`docs/architecture/`)**:
  - [01-system-architecture.md](docs/architecture/01-system-architecture.md)
  - [02-database-schema.md](docs/architecture/02-database-schema.md)
  - [03-media-ingestion-and-playback.md](docs/architecture/03-media-ingestion-and-playback.md)
  - [04-design-system-and-localization.md](docs/architecture/04-design-system-and-localization.md)
- **Architecture Decision Records (`docs/decisions/`)**:
  - [ADR-001: Manual Storage Selection via SAF](docs/decisions/ADR-001-manual-storage-selection-saf.md)
  - [ADR-002: SQLite (Room) & Thumbnail Storage](docs/decisions/ADR-002-sqlite-room-and-thumbnail-storage.md)
  - [ADR-003: Category Public/Private Access & Unrestricted Password](docs/decisions/ADR-003-category-privacy-and-password.md)
  - [ADR-004: Full-screen Image Pager & Media3 Video Player](docs/decisions/ADR-004-image-pager-and-media3-video-player.md)
  - [ADR-005: Multi-Theme Palettes, Localization & Concise About Screen](docs/decisions/ADR-005-theming-localization-and-about-screen.md)
- **Naming & Coding Standards (`docs/standards/`)**:
  - [kotlin-android-naming-conventions.md](docs/standards/kotlin-android-naming-conventions.md)
- **GitHub Workflow (`docs/github/`)**:
  - [github-workflow-guide.md](docs/github/github-workflow-guide.md)
- **7-Phase Implementation Roadmap (`docs/roadmap/`)**:
  - [implementation-phases.md](docs/roadmap/implementation-phases.md)
