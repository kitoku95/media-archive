package io.github.kitoku95.mediaarchive.domain.repository

import io.github.kitoku95.mediaarchive.domain.model.AppDarkMode
import io.github.kitoku95.mediaarchive.domain.model.AppLanguage
import io.github.kitoku95.mediaarchive.domain.model.AppThemePalette
import io.github.kitoku95.mediaarchive.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and persisting user theme and language preferences.
 */
interface UserPreferencesRepository {
    fun observeUserPreferences(): Flow<UserPreferences>
    suspend fun updateThemePalette(palette: AppThemePalette)
    suspend fun updateDarkMode(darkMode: AppDarkMode)
    suspend fun updateLanguage(language: AppLanguage)
}
