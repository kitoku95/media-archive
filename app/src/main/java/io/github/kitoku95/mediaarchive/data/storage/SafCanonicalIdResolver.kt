package io.github.kitoku95.mediaarchive.data.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves a canonical SAF identifier (`authority::documentId`) so the same physical file
 * selected via `OpenMultipleDocuments` and `OpenDocumentTree` resolves to one stable key.
 */
@Singleton
class SafCanonicalIdResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun resolve(uri: Uri): String {
        val authority = uri.authority.orEmpty()
        val isDocument = runCatching { DocumentsContract.isDocumentUri(context, uri) }.getOrDefault(false)
        if (isDocument) {
            val documentId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
            if (!documentId.isNullOrBlank()) {
                return "$authority::$documentId"
            }
        }

        val isTree = runCatching { DocumentsContract.isTreeUri(uri) }.getOrDefault(false)
        if (isTree) {
            val treeDocumentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            if (!treeDocumentId.isNullOrBlank()) {
                return "$authority::$treeDocumentId"
            }
        }

        return uri.normalizeScheme().toString()
    }
}
