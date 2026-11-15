package io.github.kitoku95.mediaarchive.domain.usecase.settings

import io.github.kitoku95.mediaarchive.domain.model.AppDarkMode
import io.github.kitoku95.mediaarchive.domain.model.AppLanguage
import io.github.kitoku95.mediaarchive.domain.model.AppThemePalette
import io.github.kitoku95.mediaarchive.domain.model.UserPreferences
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUseCasesTest {

    private val repository: UserPreferencesRepository = mockk(relaxed = true)

    @Test
    fun `observe user preferences returns repository flow`() = runTest {
        val expected = UserPreferences(
            themePalette = AppThemePalette.DEEP_FOREST,
            darkMode = AppDarkMode.DARK,
            language = AppLanguage.JAPANESE
        )
        every { repository.observeUserPreferences() } returns flowOf(expected)

        val useCase = ObserveUserPreferencesUseCase(repository)
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `update theme palette delegates to repository`() = runTest {
        val useCase = UpdateAppThemePaletteUseCase(repository)
        useCase(AppThemePalette.DEEP_CRIMSON)

        coVerify(exactly = 1) { repository.updateThemePalette(AppThemePalette.DEEP_CRIMSON) }
    }

    @Test
    fun `update dark mode delegates to repository`() = runTest {
        val useCase = UpdateAppDarkModeUseCase(repository)
        useCase(AppDarkMode.LIGHT)

        coVerify(exactly = 1) { repository.updateDarkMode(AppDarkMode.LIGHT) }
    }

    @Test
    fun `update language delegates to repository`() = runTest {
        val useCase = UpdateAppLanguageUseCase(repository)
        useCase(AppLanguage.VIETNAMESE)

        coVerify(exactly = 1) { repository.updateLanguage(AppLanguage.VIETNAMESE) }
    }
}
