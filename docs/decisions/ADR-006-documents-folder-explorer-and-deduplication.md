# ADR-006: Office Documents Tab, Explorer-Style Nested Folders, Category Card Uniformity & Canonical SAF Deduplication

- **Status**: Approved
- **Date**: 2026-11-20
- **Related Rules**: HR-01, HR-04, HR-05, HR-06, HR-08
- **Extends**: ADR-001, ADR-002

---

## 1. Context

Following the initial release of **MediaArchive** (Images and Videos organized in flat Category tabs), five product and architectural gaps were identified:

1. **Office / Text / PDF Documents**: Users want to archive and organize document files (`.txt`, `.md`, `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.csv`, `.ppt`, `.pptx`, `.rtf`, OpenDocument formats) alongside their photos and videos, and open them with installed external apps when MediaArchive does not bundle a native viewer.
2. **Subfolder Hierarchy Preservation**: Previously, `SafMediaScanner.scanFolder()` flattened every nested subfolder into a single list under `category_id`, discarding folder structure. Users expect that when they select a folder containing mixed files and subfolders, the selected folder appears as a browsable folder item and tapping into it reveals its child files and subfolders recursively (Explorer style).
3. **Dedicated Documents Tab UX**: Documents do not fit a 1:1 visual thumbnail grid. They require a dedicated **3rd tab ("Documents")** rendered as a uniform-height vertical list with clear file-extension differentiation (`.txt`, `.pdf`, `.docx`, `.xlsx`, `.pptx`, etc.) and single-line ellipsis truncation (`...`) for long filenames.
4. **Category Card Height Consistency**: On `CategoryListScreen`, the default `Main` card omitted the trailing `48.dp` delete `IconButton`, resulting in a shorter card (~`52.dp`) than custom `Public`/`Private` Category cards (~`76.dp`).
5. **Cross-URI SAF Deduplication**: While `media_items` enforced `UNIQUE(category_id, content_uri)`, Android's Storage Access Framework generates **different `content_uri` strings** for the same physical file depending on whether it was picked via `OpenMultipleDocuments` (`.../document/primary:A/file.jpg`) or discovered inside an `OpenDocumentTree` (`.../tree/primary:A/document/primary:A/file.jpg`). Additionally, insert conflicts (`-1L`) could leak newly generated `.webp` thumbnails on disk, and skipped duplicates were not surfaced to the user.

---

## 2. Decision

### 2.1. `MediaType.DOCUMENT` & External App Delegation (`ACTION_VIEW`)
- Extend `MediaType` enum to `IMAGE`, `VIDEO`, `DOCUMENT`.
- Expand `OpenMultipleDocuments` MIME filters to include:
  - `image/*`, `video/*`, `text/*`, `application/pdf`, `application/rtf`
  - Word: `application/msword`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `application/vnd.oasis.opendocument.text`
  - Excel: `application/vnd.ms-excel`, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, `application/vnd.oasis.opendocument.spreadsheet`
  - PowerPoint: `application/vnd.ms-powerpoint`, `application/vnd.openxmlformats-officedocument.presentationml.presentation`, `application/vnd.oasis.opendocument.presentation`
- Classify files by MIME type first, with a lowercase file-extension fallback (`.txt`, `.md`, `.csv`, `.json`, `.xml`, `.rtf`, `.pdf`, `.doc`, `.docx`, `.odt`, `.xls`, `.xlsx`, `.ods`, `.ppt`, `.pptx`, `.odp`) to support SAF providers that return `application/octet-stream`.
- Skip `.webp` thumbnail generation for `MediaType.DOCUMENT` (`thumbnail_path = ""`).
- Open documents via an `ExternalDocumentOpener` helper using `Intent(Intent.ACTION_VIEW)` with `setDataAndType(uri, mimeType)` and `Intent.FLAG_GRANT_READ_URI_PERMISSION`, wrapped in `Intent.createChooser(...)`. Catch `ActivityNotFoundException` and `SecurityException` to display a localized Snackbar error.

### 2.2. Flat Recursive Folder Import & Hidden/Cache Folder Filtering *(Amended after real-device testing)*
- Upgrade `MediaArchiveDatabase` to **version 3** via `MIGRATION_1_2` and `MIGRATION_2_3` (flattening any v2 subfolder items directly into their owning Category and dropping `archive_folders`).
- **Folder Import Behavior**: Selecting a folder via `OpenDocumentTree` recursively scans the folder and all nested subfolders and flattens all supported Images, Videos, and Documents directly into the Category's 3 tabs (no subfolder cards/rows or breadcrumb UI inside a Category).
- **Hidden/Cache Exclusion**: During recursive folder traversal, skip hidden/system cache folders whose names start with `.` (e.g., `.thumbnails`, `.trash`, `.cache`) or match `thumbnails` (case-insensitive), as well as dot-prefixed hidden files (e.g., `.nomedia`, `.trashed-*`), to avoid importing low-resolution cache thumbnails.

### 2.3. 3rd Tab ("Documents") & Uniform List / Card Dimensions
- Upgrade `CategoryDetailScreen` `PrimaryTabRow` to **3 tabs**: **Images**, **Videos**, and **Documents**.
- **Documents Tab**:
  - Uses a vertical `LazyColumn` (not a square grid).
  - Every row (`DocumentListItem` and `FolderListItem`) has an identical fixed height (`Modifier.fillMaxWidth().height(72.dp)`).
  - Each `DocumentListItem` displays a `44.dp x 44.dp` color-coded extension badge (e.g., `PDF`, `DOCX`, `XLSX`, `PPTX`, `TXT`) on the left, a single-line `displayName` with `maxLines = 1` and `TextOverflow.Ellipsis`, and a single-line metadata subtitle (`EXT • File Size`).
- **Category List Screen (`CategoryCard`)**:
  - Standardize `CategoryCard`'s inner `Row` to a fixed height (`Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp)`) with `Alignment.CenterVertically`, ensuring the default `Main` card and all user-created `Public`/`Private` cards have identical visual height.

### 2.4. Canonical SAF Deduplication (`canonical_id`)
- Introduce `SafCanonicalIdResolver.resolve(context, uri)`:
  - When `DocumentsContract.isDocumentUri(context, uri)` is true (covering both `OpenMultipleDocuments` and `OpenDocumentTree` children): return `"${uri.authority}::${DocumentsContract.getDocumentId(uri)}"`.
  - When `DocumentsContract.isTreeUri(uri)` is true: return `"${uri.authority}::${DocumentsContract.getTreeDocumentId(uri)}"`.
  - Fallback: normalized `uri.toString()`.
- Store `canonical_id` on both `media_items` and `archive_folders` and enforce `UNIQUE(category_id, canonical_id)`.
- Deduplicate incoming URI batches by `canonical_id` before thumbnail extraction, check `existsByCanonicalId(categoryId, canonicalId)`, and immediately delete any newly created `.webp` thumbnail if Room returns `-1L` (`OnConflictStrategy.IGNORE`).
- Return `ImportResult(importedCount: Int, skippedDuplicateCount: Int)` from import UseCases so the UI can notify the user when duplicates are skipped.

---

## 3. Consequences

### Positive:
- Full Explorer-like folder browsing without violating **HR-01** (strictly manual SAF selection; zero background storage scanning).
- Zero APK size bloat for Office/PDF rendering by delegating to the user's preferred installed Android apps via `ACTION_VIEW` with scoped read URI grants.
- Eliminates cross-SAF-URI duplicate imports and prevents orphan `.webp` thumbnail leaks.
- Achieves uniform row/card geometry across both `CategoryListScreen` and the `Documents` tab.

### Technical Considerations:
- Room `MIGRATION_1_2` must recreate `media_items` cleanly to backfill `canonical_id = content_uri` for existing v1 rows without data loss.
- Taking persistable read permission on a selected folder's `treeUri` covers all nested child files and subfolders while consuming only a single OS persistable URI permission slot.
