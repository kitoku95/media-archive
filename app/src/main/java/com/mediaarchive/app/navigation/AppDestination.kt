package com.mediaarchive.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for MediaArchive.
 */
sealed interface AppDestination {
    @Serializable
    data object CategoryList : AppDestination

    @Serializable
    data object Settings : AppDestination

    @Serializable
    data object About : AppDestination
}
