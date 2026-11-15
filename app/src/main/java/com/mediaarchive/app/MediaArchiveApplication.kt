package com.mediaarchive.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point enabling Hilt dependency injection.
 */
@HiltAndroidApp
class MediaArchiveApplication : Application()
