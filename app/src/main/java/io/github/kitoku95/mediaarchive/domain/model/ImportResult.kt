package io.github.kitoku95.mediaarchive.domain.model

/**
 * Summary of a manual SAF import operation, including skipped duplicate count.
 */
data class ImportResult(
    val importedCount: Int,
    val skippedDuplicateCount: Int
)
