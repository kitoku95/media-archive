package io.github.kitoku95.mediaarchive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.feature.about.AboutScreen
import io.github.kitoku95.mediaarchive.feature.categorydetail.CategoryDetailRoute
import io.github.kitoku95.mediaarchive.feature.categorylist.CategoryListRoute
import io.github.kitoku95.mediaarchive.feature.imageviewer.ImageViewerRoute
import io.github.kitoku95.mediaarchive.feature.settings.SettingsRoute
import io.github.kitoku95.mediaarchive.feature.videoplayer.VideoPlayerRoute

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
                            isPrivate = category.accessMode == AccessMode.PRIVATE,
                            categoryType = category.categoryType.name
                        )
                    )
                },
                onNavigateToSettings = { navController.navigate(AppDestination.Settings) },
                onNavigateToAbout = { navController.navigate(AppDestination.About) }
            )
        }

        composable<AppDestination.CategoryDetail> {
            CategoryDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImageViewer = { categoryId, initialIndex, isPrivate ->
                    navController.navigate(
                        AppDestination.ImageViewer(
                            categoryId = categoryId,
                            initialIndex = initialIndex,
                            isPrivate = isPrivate
                        )
                    )
                },
                onNavigateToVideoPlayer = { categoryId, initialIndex, isPrivate ->
                    navController.navigate(
                        AppDestination.VideoPlayer(
                            categoryId = categoryId,
                            initialIndex = initialIndex,
                            isPrivate = isPrivate
                        )
                    )
                }
            )
        }

        composable<AppDestination.ImageViewer> {
            ImageViewerRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AppDestination.VideoPlayer> {
            VideoPlayerRoute(
                onNavigateBack = { navController.popBackStack() }
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
