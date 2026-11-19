package io.github.kitoku95.mediaarchive.data.storage

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Outcome of attempting to open a document in an installed external Android app.
 */
sealed interface DocumentOpenOutcome {
    data object Opened : DocumentOpenOutcome
    data object NoAppFound : DocumentOpenOutcome
    data object FileInaccessible : DocumentOpenOutcome
}

/**
 * Delegates document viewing (`MediaType.DOCUMENT`) to installed external apps via `ACTION_VIEW`
 * with scoped read URI permission.
 */
@Singleton
class ExternalDocumentOpener @Inject constructor() {

    fun openDocument(
        context: Context,
        contentUri: String,
        mimeType: String,
        displayName: String
    ): DocumentOpenOutcome {
        val uri = Uri.parse(contentUri)
        val resolvedMimeType = mimeType.ifBlank { "*/*" }
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, resolvedMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(viewIntent, displayName).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(chooserIntent)
            DocumentOpenOutcome.Opened
        } catch (_: ActivityNotFoundException) {
            DocumentOpenOutcome.NoAppFound
        } catch (_: SecurityException) {
            DocumentOpenOutcome.FileInaccessible
        }
    }
}
