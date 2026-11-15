package io.github.kitoku95.mediaarchive.domain.usecase.settings

import io.github.kitoku95.mediaarchive.domain.model.AppLanguage
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Persists the user's selected display language.
 */
class UpdateAppLanguageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(language: AppLanguage) {
        userPreferencesRepository.updateLanguage(language)
    }
}
