# ADR-002: Hybrid Thumbnail File Caching & SQLite (Room) Path Storage

- **Status**: Approved
- **Date**: 2026-11-15
- **Related Rules**: HR-04, HR-06, HR-08

---

## 1. Context

When the user selects individual media files or a folder containing images and videos, **MediaArchive** must:
- Save both the **media path** and its **thumbnail** in the database (SQLite).
- Render the Category's Images and Videos in a responsive 1:1 Square Grid.

Storing raw thumbnail image bytes (`BLOB`) directly inside SQLite rows causes severe performance bottlenecks on Android (`CursorWindow` has a hard 2MB buffer limit and can throw `SQLiteBlobTooBigException` when scrolling through hundreds of items).
By writing compressed `.webp` thumbnail files into the app's private internal directory (`context.filesDir/thumbnails/<uuid>.webp`) and storing both `content_uri` and `thumbnail_path` in SQLite:
- SQLite queries remain lightweight and fast.
- Image loaders (Coil) read cached local `.webp` thumbnails at 60–120 FPS.

---

## 2. Decision

1. **Use AndroidX Room (SQLite)** with two core tables: `categories` and `media_items`.
2. **Hybrid File + SQLite Thumbnail Persistence**:
   - During media or folder ingestion:
     - Extract a `512x512 px` thumbnail bitmap.
     - Compress it as `WEBP` (quality 80) and write it to `context.filesDir/thumbnails/<uuid>.webp`.
     - Insert both `content_uri` (source file URI) and `thumbnail_path` (local thumbnail file path) into the `media_items` SQLite table.
3. **Privacy & Lifecycle Synchronization**:
   - Create a `.nomedia` file inside `context.filesDir/thumbnails/`.
   - Delete associated `.webp` files whenever a media item or Category is deleted.

---

## 3. Consequences

### Positive:
- Fulfils the requirement to extract and persist both media paths and thumbnails upon selection.
- Eliminates `CursorWindow` memory limits when scrolling large media grids.
- Keeps thumbnails of `private` Categories isolated inside the app's internal sandbox (`context.filesDir`).
