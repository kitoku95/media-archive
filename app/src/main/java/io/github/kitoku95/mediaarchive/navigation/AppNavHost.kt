package io.github.kitoku95.mediaarchive.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.feature.about.AboutScreen
import io.github.kitoku95.mediaarchive.feature.categorydetail.CategoryDetailRoute
import io.github.kitoku95.mediaarchive.feature.categorylist.CategoryListRoute
import io.github.kitoku95.mediaarchive.feature.settings.SettingsRoute

/**
 * Centralized navigation graph connecting CategoryList, CategoryDetail, Settings, and About screens.
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
            CategoryListRoute(
                onNavigateToCategoryDetail = { category ->
                    navController.navigate(
                        AppDestination.CategoryDetail(
                            categoryId = category.id,
                            categoryName = category.name,
                            isPrivate = category.accessMode == AccessMode.PRIVATE
                        )
                    )
                },
                onNavigateToSettings = { navController.navigate(AppDestination.Settings) },
                onNavigateToAbout = { navController.navigate(AppDestination.About) }
            )
        }

        composable<AppDestination.CategoryDetail> {
            val context = LocalContext.current
            CategoryDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImageViewer = { _, _ ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.category_detail_viewer_coming_soon),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onNavigateToVideoPlayer = { _, _ ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.category_detail_viewer_coming_soon),
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
