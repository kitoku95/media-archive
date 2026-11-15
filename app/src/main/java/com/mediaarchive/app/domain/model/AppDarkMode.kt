package com.mediaarchive.app.domain.model

/**
 * Supported appearance modes for light/dark rendering.
 */
enum class AppDarkMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(value: String?): AppDarkMode {
            return entries.firstOrNull { it.name == value } ?: SYSTEM
        }
    }
}
