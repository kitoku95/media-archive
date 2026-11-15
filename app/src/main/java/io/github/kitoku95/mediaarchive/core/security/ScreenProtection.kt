package io.github.kitoku95.mediaarchive.core.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Applies [WindowManager.LayoutParams.FLAG_SECURE] while viewing a `PRIVATE` category
 * to block screenshots and hide content in the Recent Apps switcher.
 */
@Composable
fun PrivateScreenProtectionEffect(isPrivate: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(isPrivate, view) {
        val window = context.findActivity()?.window
        if (isPrivate) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (isPrivate) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
