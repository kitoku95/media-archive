package com.mediaarchive.app.domain.repository

import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette
import com.mediaarchive.app.domain.model.UserPreferences
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
