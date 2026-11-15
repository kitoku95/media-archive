package io.github.kitoku95.mediaarchive.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.feature.about.AboutScreen
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

        composable<AppDestination.CategoryDetail> { backStackEntry ->
            val destination = backStackEntry.toRoute<AppDestination.CategoryDetail>()
            CategoryDetailPlaceholderScreen(
                categoryName = destination.categoryName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetailPlaceholderScreen(
    categoryName: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = categoryName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.category_detail_placeholder_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
