# Android Architecture, Coding & Security Hard Rules

This document specifies the mandatory technical, architectural, and security rules for **MediaArchive**.

---

## 1. Documentation Language Rule

- **100% English Documentation**: All Markdown (`.md`) files, Architecture Decision Records (ADRs), code comments, KDoc, and commit messages in the repository **MUST** be written in English.

---

## 2. Architecture Rules (Clean Architecture + MVVM / UDF)

1. **Strict 3-Layer Separation (`Presentation` -> `Domain` <- `Data`)**:
   - **Domain Layer (`domain/`)**: Pure Kotlin and Coroutines `Flow` only. **Never** import `android.*`, `androidx.compose.*`, or `androidx.room.*` in the Domain layer.
   - **Data Layer (`data/`)**: Contains Room Entities, DAOs, DataStore Preferences, SAF Scanners, Thumbnail Generators, and Repository Implementations. Always map `Entity` models to `Domain` models before returning them to Domain/Presentation.
   - **Presentation Layer (`feature/`)**: Built with Jetpack Compose and `ViewModel`. Screens receive immutable `UiState` and emit actions via `onEvent: (UiEvent) -> Unit`.
2. **No `Context` or `Activity` inside `ViewModel`**:
   - Operations requiring `Context` (`ContentResolver`, thumbnail decoding, file I/O, locale management) belong in the `Data` or `Core` layer injected with `@ApplicationContext` via **Hilt**.

---

## 3. Concurrency & Performance Rules

1. **Main-Safety**:
   - All SQLite queries, SAF directory traversals (`DocumentFile`), bitmap decoding, and thumbnail file writes **MUST** execute on `Dispatchers.IO`.
2. **Smooth Square Grid Rendering**:
   - `LazyVerticalGrid` must always specify `key = { item -> item.id }` and `contentType = { item -> item.mediaType }`.
   - Grid items must load only the pre-generated `thumbnailPath` file, never decoding full-resolution original videos or images on the grid.
3. **Media3 `ExoPlayer` Lifecycle**:
   - Maintain a single `ExoPlayer` instance on the Video Player screen, pause playback on `ON_STOP`, and call `release()` when leaving the screen.

---

## 4. Source Code Security Hard Rules

Even though the app operates on local device storage, the source code must pass strict security standards:

1. **Password Hashing for `private` Categories**:
   - Never store plain-text passwords in SQLite or DataStore.
   - Hash passwords using `PBKDF2WithHmacSHA256` (or `SHA-256` with a unique 16-byte `SecureRandom` salt per Category).
   - Verify password hashes using constant-time comparison (`MessageDigest.isEqual`).
2. **Private Thumbnail Sandbox & `FLAG_SECURE`**:
   - Store all generated thumbnails inside `context.filesDir/thumbnails/` (app-private internal storage) with a `.nomedia` marker file.
   - Enable `WindowManager.LayoutParams.FLAG_SECURE` whenever the user is inside a `PRIVATE` Category (Grid, Image Viewer, and Video Player) to prevent screenshots and hide previews in the Recent Apps switcher.
3. **SQL Injection & Path Traversal Prevention**:
   - Use parameterized Room queries (`:categoryId`, `:mediaType`) exclusively; never concatenate raw SQL strings.
   - Name thumbnail files strictly using generated UUIDs (`<uuid>.webp`); never construct file paths using untrusted `displayName` strings from SAF URIs.
4. **Manifest & Release Build Hardening**:
   - Set `android:allowBackup="false"` and configure `dataExtractionRules` in `AndroidManifest.xml` to prevent extracting SQLite databases and thumbnails via `adb backup`.
   - Keep `android:exported="false"` on all components except `MainActivity`.
   - Strip debug logs containing URIs or sensitive data in Release builds via R8/ProGuard.
