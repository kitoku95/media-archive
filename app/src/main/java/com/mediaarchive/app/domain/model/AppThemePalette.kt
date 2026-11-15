package com.mediaarchive.app.domain.model

/**
 * Supported color palettes for the application UI.
 */
enum class AppThemePalette {
    NEUTRAL_TAUPE,
    DEEP_CRIMSON,
    DEEP_FOREST,
    DEEP_SAPPHIRE;

    companion object {
        fun fromName(value: String?): AppThemePalette {
            return entries.firstOrNull { it.name == value } ?: NEUTRAL_TAUPE
        }
    }
}
