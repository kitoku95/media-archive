package io.github.kitoku95.mediaarchive.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.kitoku95.mediaarchive.domain.model.AppDarkMode
import io.github.kitoku95.mediaarchive.domain.model.AppThemePalette

/**
 * Root Material 3 theme supporting all 4 MediaArchive palettes and light/dark appearance modes.
 */
@Composable
fun MediaArchiveTheme(
    palette: AppThemePalette = AppThemePalette.NEUTRAL_TAUPE,
    darkMode: AppDarkMode = AppDarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (darkMode) {
        AppDarkMode.SYSTEM -> isSystemInDarkTheme()
        AppDarkMode.LIGHT -> false
        AppDarkMode.DARK -> true
    }

    val colorScheme = when (palette) {
        AppThemePalette.NEUTRAL_TAUPE -> {
            if (isDark) NeutralTaupeDarkColorScheme else NeutralTaupeLightColorScheme
        }
        AppThemePalette.DEEP_CRIMSON -> {
            if (isDark) DeepCrimsonDarkColorScheme else DeepCrimsonLightColorScheme
        }
        AppThemePalette.DEEP_FOREST -> {
            if (isDark) DeepForestDarkColorScheme else DeepForestLightColorScheme
        }
        AppThemePalette.DEEP_SAPPHIRE -> {
            if (isDark) DeepSapphireDarkColorScheme else DeepSapphireLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MediaArchiveTypography,
        content = content
    )
}
