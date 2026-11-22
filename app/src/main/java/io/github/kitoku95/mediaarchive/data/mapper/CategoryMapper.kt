package io.github.kitoku95.mediaarchive.data.mapper

import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    categoryType = if (categoryType == CategoryType.DOCUMENT.name) {
        CategoryType.DOCUMENT
    } else {
        CategoryType.MEDIA
    },
    accessMode = if (accessMode == AccessMode.PRIVATE.name) {
        AccessMode.PRIVATE
    } else {
        AccessMode.PUBLIC
    },
    isDefault = isDefault,
    createdAt = createdAt
)
