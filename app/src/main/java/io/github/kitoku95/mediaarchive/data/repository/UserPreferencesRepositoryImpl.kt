package io.github.kitoku95.mediaarchive.data.repository

import io.github.kitoku95.mediaarchive.core.localization.AppLocaleManager
import io.github.kitoku95.mediaarchive.data.local.preferences.UserPreferencesDataSource
import io.github.kitoku95.mediaarchive.domain.model.AppDarkMode
import io.github.kitoku95.mediaarchive.domain.model.AppLanguage
import io.github.kitoku95.mediaarchive.domain.model.AppThemePalette
import io.github.kitoku95.mediaarchive.domain.model.UserPreferences
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
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
