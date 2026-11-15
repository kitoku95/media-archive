package io.github.kitoku95.mediaarchive.domain.usecase.settings

import io.github.kitoku95.mediaarchive.domain.model.AppDarkMode
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
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
