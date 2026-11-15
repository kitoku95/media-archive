package com.mediaarchive.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette
import com.mediaarchive.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed source of truth for user appearance and language preferences.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val THEME_PALETTE = stringPreferencesKey("app_theme_palette")
        val DARK_MODE = stringPreferencesKey("app_dark_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themePalette = AppThemePalette.fromName(prefs[PreferencesKeys.THEME_PALETTE]),
            darkMode = AppDarkMode.fromName(prefs[PreferencesKeys.DARK_MODE]),
            language = AppLanguage.fromTag(prefs[PreferencesKeys.LANGUAGE])
        )
    }

    suspend fun updateThemePalette(palette: AppThemePalette) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_PALETTE] = palette.name
        }
    }

    suspend fun updateDarkMode(darkMode: AppDarkMode) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE] = darkMode.name
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LANGUAGE] = language.tag
        }
    }
}
