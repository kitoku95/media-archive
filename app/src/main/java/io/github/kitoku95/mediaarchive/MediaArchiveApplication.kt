package io.github.kitoku95.mediaarchive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point enabling Hilt dependency injection and ensuring the default
 * `Main` Category is present in SQLite.
 */
@HiltAndroidApp
class MediaArchiveApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            categoryRepository.ensureDefaultCategoryExists()
        }
    }
}
