package com.mediaarchive.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaarchive.app.domain.usecase.settings.ObserveUserPreferencesUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppDarkModeUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppLanguageUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppThemePaletteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages user theme, appearance mode, and language selection on the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val updateAppThemePaletteUseCase: UpdateAppThemePaletteUseCase,
    private val updateAppDarkModeUseCase: UpdateAppDarkModeUseCase,
    private val updateAppLanguageUseCase: UpdateAppLanguageUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observeUserPreferencesUseCase()
        .map { prefs ->
            SettingsUiState(
                selectedPalette = prefs.themePalette,
                selectedDarkMode = prefs.darkMode,
                selectedLanguage = prefs.language
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    private val _uiEffect = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SettingsUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnPaletteSelected -> {
                viewModelScope.launch {
                    updateAppThemePaletteUseCase(event.palette)
                }
            }
            is SettingsUiEvent.OnDarkModeSelected -> {
                viewModelScope.launch {
                    updateAppDarkModeUseCase(event.darkMode)
                }
            }
            is SettingsUiEvent.OnLanguageSelected -> {
                viewModelScope.launch {
                    updateAppLanguageUseCase(event.language)
                }
            }
            SettingsUiEvent.OnAboutClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(SettingsUiEffect.NavigateToAbout)
                }
            }
        }
    }
}
