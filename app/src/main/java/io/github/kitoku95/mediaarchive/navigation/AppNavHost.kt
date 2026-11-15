package io.github.kitoku95.mediaarchive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.kitoku95.mediaarchive.feature.about.AboutScreen
import io.github.kitoku95.mediaarchive.feature.categorylist.CategoryListPlaceholderScreen
import io.github.kitoku95.mediaarchive.feature.settings.SettingsRoute

/**
 * Centralized navigation graph connecting CategoryList, Settings, and About screens.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.CategoryList,
        modifier = modifier
    ) {
        composable<AppDestination.CategoryList> {
            CategoryListPlaceholderScreen(
                onNavigateToSettings = { navController.navigate(AppDestination.Settings) },
                onNavigateToAbout = { navController.navigate(AppDestination.About) }
            )
        }

        composable<AppDestination.Settings> {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(AppDestination.About) }
            )
        }

        composable<AppDestination.About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
