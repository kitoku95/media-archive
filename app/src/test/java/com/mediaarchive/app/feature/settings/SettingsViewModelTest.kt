package com.mediaarchive.app.feature.settings

import app.cash.turbine.test
import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette
import com.mediaarchive.app.domain.model.UserPreferences
import com.mediaarchive.app.domain.repository.UserPreferencesRepository
import com.mediaarchive.app.domain.usecase.settings.ObserveUserPreferencesUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppDarkModeUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppLanguageUseCase
import com.mediaarchive.app.domain.usecase.settings.UpdateAppThemePaletteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeUserPreferencesRepository()
        viewModel = SettingsViewModel(
            observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(fakeRepository),
            updateAppThemePaletteUseCase = UpdateAppThemePaletteUseCase(fakeRepository),
            updateAppDarkModeUseCase = UpdateAppDarkModeUseCase(fakeRepository),
            updateAppLanguageUseCase = UpdateAppLanguageUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting palette, dark mode, and language updates uiState`() = runTest {
        viewModel.uiState.test {
            assertEquals(SettingsUiState(), awaitItem())

            viewModel.onEvent(SettingsUiEvent.OnPaletteSelected(AppThemePalette.DEEP_SAPPHIRE))
            advanceUntilIdle()
            assertEquals(AppThemePalette.DEEP_SAPPHIRE, awaitItem().selectedPalette)

            viewModel.onEvent(SettingsUiEvent.OnDarkModeSelected(AppDarkMode.DARK))
            advanceUntilIdle()
            assertEquals(AppDarkMode.DARK, awaitItem().selectedDarkMode)

            viewModel.onEvent(SettingsUiEvent.OnLanguageSelected(AppLanguage.VIETNAMESE))
            advanceUntilIdle()
            assertEquals(AppLanguage.VIETNAMESE, awaitItem().selectedLanguage)
        }
    }

    @Test
    fun `clicking about emits NavigateToAbout effect`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.OnAboutClicked)
            advanceUntilIdle()
            assertEquals(SettingsUiEffect.NavigateToAbout, awaitItem())
        }
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val state = MutableStateFlow(UserPreferences())

        override fun observeUserPreferences(): Flow<UserPreferences> = state

        override suspend fun updateThemePalette(palette: AppThemePalette) {
            state.value = state.value.copy(themePalette = palette)
        }

        override suspend fun updateDarkMode(darkMode: AppDarkMode) {
            state.value = state.value.copy(darkMode = darkMode)
        }

        override suspend fun updateLanguage(language: AppLanguage) {
            state.value = state.value.copy(language = language)
        }
    }
}
