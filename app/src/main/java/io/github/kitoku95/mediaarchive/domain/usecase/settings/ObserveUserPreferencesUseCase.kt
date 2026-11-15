package io.github.kitoku95.mediaarchive.domain.usecase.settings

import io.github.kitoku95.mediaarchive.domain.model.UserPreferences
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
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
