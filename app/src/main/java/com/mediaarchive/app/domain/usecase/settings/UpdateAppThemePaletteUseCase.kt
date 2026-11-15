package com.mediaarchive.app.domain.usecase.settings

import com.mediaarchive.app.domain.model.AppThemePalette
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Persists the user's selected color theme palette.
 */
class UpdateAppThemePaletteUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(palette: AppThemePalette) {
        userPreferencesRepository.updateThemePalette(palette)
    }
}
