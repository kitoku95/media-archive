package io.github.kitoku95.mediaarchive.domain.model

/**
 * Immutable snapshot of user-configurable appearance and language preferences.
 */
data class UserPreferences(
    val themePalette: AppThemePalette = AppThemePalette.NEUTRAL_TAUPE,
    val darkMode: AppDarkMode = AppDarkMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH
)
