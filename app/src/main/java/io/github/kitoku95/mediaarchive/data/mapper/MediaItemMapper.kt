package io.github.kitoku95.mediaarchive.data.mapper

import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType

fun MediaItemEntity.toDomain(): MediaItem = MediaItem(
    id = id,
    categoryId = categoryId,
    mediaType = if (mediaType == MediaType.VIDEO.name) {
        MediaType.VIDEO
    } else {
        MediaType.IMAGE
    },
    contentUri = contentUri,
    thumbnailPath = thumbnailPath,
    displayName = displayName,
    mimeType = mimeType,
    durationMs = durationMs,
    sizeBytes = sizeBytes,
    addedAt = addedAt
)
