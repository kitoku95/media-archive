package com.mediaarchive.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaarchive.app.domain.model.UserPreferences
import com.mediaarchive.app.domain.usecase.settings.ObserveUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes persisted user theme and appearance preferences to [MainActivity].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeUserPreferencesUseCase: ObserveUserPreferencesUseCase
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = observeUserPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )
}
