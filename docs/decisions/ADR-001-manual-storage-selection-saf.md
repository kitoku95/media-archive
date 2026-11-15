# ADR-001: Manual Storage Selection Using Storage Access Framework (SAF)

- **Status**: Approved
- **Date**: 2026-11-15
- **Related Rules**: HR-01, HR-06

---

## 1. Context

The core requirements of **MediaArchive** state that:
1. The app **must not automatically scan device storage** on startup or in the background; instead, the user manually taps **"Browse Storage"** to select individual images/videos or an entire folder from internal or external storage.
2. Selected media paths must be persisted in SQLite so the user can view them across app restarts without re-selecting them.

On modern Android (Android 10+ Scoped Storage and Android 13+ Granular Media Permissions):
- Requesting broad permissions (`READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`) is designed for apps that scan the entire `MediaStore`.
- Conversely, Android's **Storage Access Framework (SAF)** lets the user explicitly grant access to specific documents (`ACTION_OPEN_DOCUMENT`) or directory trees (`ACTION_OPEN_DOCUMENT_TREE`) without requiring broad storage permissions in `AndroidManifest.xml`.

---

## 2. Decision

1. **Use Storage Access Framework (SAF) exclusively** for importing media:
   - Single or multiple individual files: `ActivityResultContracts.OpenMultipleDocuments()` filtered with `["image/*", "video/*"]`.
   - Folder selection: `ActivityResultContracts.OpenDocumentTree()`.
2. **Persist URI Read Permissions**:
   - Immediately upon receiving a `Uri` from the SAF picker, invoke:
     ```kotlin
     contentResolver.takePersistableUriPermission(
         uri,
         Intent.FLAG_GRANT_READ_URI_PERMISSION
     )
     ```
   - This guarantees that `content://...` URIs saved in SQLite remain readable after device reboots.
3. **Folder Traversal**:
   - When a user selects a folder (`treeUri`), traverse child documents using `DocumentFile.fromTreeUri(context, treeUri)` (or `DocumentsContract.buildChildDocumentsUriUsingTree`) to discover all files whose `mimeType` starts with `image/` or `video/`.

---

## 3. Consequences

### Positive:
- 100% compliance with HR-01 (*no automatic storage scanning*).
- Maximum user privacy: the application can only read files and folders explicitly chosen by the user.
- Seamless support for internal storage, SD cards, and USB OTG drives.

### Technical Considerations:
- Taking persistable permission on a parent `treeUri` automatically covers all child media files inside that folder while consuming only 1 persistable URI permission slot from the OS limit.
