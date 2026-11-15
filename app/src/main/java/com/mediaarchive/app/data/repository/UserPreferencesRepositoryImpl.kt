package com.mediaarchive.app.data.repository

import com.mediaarchive.app.core.localization.AppLocaleManager
import com.mediaarchive.app.data.local.preferences.UserPreferencesDataSource
import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette
import com.mediaarchive.app.domain.model.UserPreferences
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation coordinating DataStore persistence and runtime locale updates.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
    private val localeManager: AppLocaleManager
) : UserPreferencesRepository {

    override fun observeUserPreferences(): Flow<UserPreferences> = dataSource.userPreferences

    override suspend fun updateThemePalette(palette: AppThemePalette) {
        dataSource.updateThemePalette(palette)
    }

    override suspend fun updateDarkMode(darkMode: AppDarkMode) {
        dataSource.updateDarkMode(darkMode)
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        dataSource.updateLanguage(language)
        localeManager.applyLanguage(language)
    }
}
