package com.mediaarchive.app.domain.usecase.settings

import com.mediaarchive.app.domain.model.UserPreferences
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes reactive updates to the user's theme and language preferences.
 */
class ObserveUserPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> = userPreferencesRepository.observeUserPreferences()
}
