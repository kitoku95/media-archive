package io.github.kitoku95.mediaarchive.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for MediaArchive.
 */
sealed interface AppDestination {
    @Serializable
    data object CategoryList : AppDestination

    @Serializable
    data class CategoryDetail(
        val categoryId: Long,
        val categoryName: String,
        val isPrivate: Boolean
    ) : AppDestination

    @Serializable
    data object Settings : AppDestination

    @Serializable
    data object About : AppDestination
}
