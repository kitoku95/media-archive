package com.mediaarchive.app.core.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mediaarchive.app.domain.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies per-app locale changes at runtime via AndroidX AppCompat.
 */
@Singleton
class AppLocaleManager @Inject constructor() {

    /**
     * Updates the active application locale on the main thread so Compose resources recompose.
     */
    suspend fun applyLanguage(language: AppLanguage) {
        withContext(Dispatchers.Main.immediate) {
            val locales = LocaleListCompat.forLanguageTags(language.tag)
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    /**
     * Resolves the currently active app language tag, falling back to English.
     */
    fun getCurrentLanguage(): AppLanguage {
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return AppLanguage.fromTag(currentTags)
    }
}
