package io.github.kitoku95.mediaarchive.domain.usecase.settings

import io.github.kitoku95.mediaarchive.domain.model.AppThemePalette
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
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
