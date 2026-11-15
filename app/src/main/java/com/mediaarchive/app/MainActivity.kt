package com.mediaarchive.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediaarchive.app.core.designsystem.MediaArchiveTheme
import com.mediaarchive.app.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host extending [AppCompatActivity] to support runtime per-app locales.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
            MediaArchiveTheme(
                palette = preferences.themePalette,
                darkMode = preferences.darkMode
            ) {
                AppNavHost()
            }
        }
    }
}
