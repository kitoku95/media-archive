package com.mediaarchive.app.feature.settings

import androidx.compose.runtime.Immutable
import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette

@Immutable
data class SettingsUiState(
    val selectedPalette: AppThemePalette = AppThemePalette.NEUTRAL_TAUPE,
    val selectedDarkMode: AppDarkMode = AppDarkMode.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH
)

sealed interface SettingsUiEvent {
    data class OnPaletteSelected(val palette: AppThemePalette) : SettingsUiEvent
    data class OnDarkModeSelected(val darkMode: AppDarkMode) : SettingsUiEvent
    data class OnLanguageSelected(val language: AppLanguage) : SettingsUiEvent
    data object OnAboutClicked : SettingsUiEvent
}

sealed interface SettingsUiEffect {
    data object NavigateToAbout : SettingsUiEffect
}
