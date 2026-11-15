package com.mediaarchive.app.domain.usecase.settings

import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
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
