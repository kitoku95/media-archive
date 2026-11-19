package io.github.kitoku95.mediaarchive.domain.model

/**
 * Represents an image, video, or document assigned to a [Category], backed by a persistable SAF URI
 * and (for images/videos) a cached local thumbnail file.
 */
data class MediaItem(
    val id: Long,
    val categoryId: Long,
    val mediaType: MediaType,
    val contentUri: String,
    val thumbnailPath: String,
    val displayName: String,
    val mimeType: String,
    val durationMs: Long?,
    val sizeBytes: Long,
    val addedAt: Long
)
