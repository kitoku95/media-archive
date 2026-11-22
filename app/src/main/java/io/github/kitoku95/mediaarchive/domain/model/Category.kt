package io.github.kitoku95.mediaarchive.domain.model

/**
 * Represents a user-selected or default Category that organizes media items.
 */
data class Category(
    val id: Long,
    val name: String,
    val categoryType: CategoryType = CategoryType.MEDIA,
    val accessMode: AccessMode,
    val isDefault: Boolean,
    val createdAt: Long
)
