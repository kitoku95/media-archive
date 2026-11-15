package io.github.kitoku95.mediaarchive.domain.model

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
