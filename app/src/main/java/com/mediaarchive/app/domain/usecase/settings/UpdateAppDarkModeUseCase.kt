package com.mediaarchive.app.domain.usecase.settings

import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Persists the user's selected light/dark appearance mode.
 */
class UpdateAppDarkModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(darkMode: AppDarkMode) {
        userPreferencesRepository.updateDarkMode(darkMode)
    }
}
