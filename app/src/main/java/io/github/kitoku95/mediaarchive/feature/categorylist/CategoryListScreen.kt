package io.github.kitoku95.mediaarchive.feature.categorylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.feature.categorylist.component.CategoryCard
import io.github.kitoku95.mediaarchive.feature.categorylist.component.CreateCategoryDialog
import io.github.kitoku95.mediaarchive.feature.categorylist.component.PasswordPromptDialog

@Composable
fun CategoryListRoute(
    onNavigateToCategoryDetail: (Category) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CategoryListUiEffect.NavigateToCategoryDetail -> {
                    onNavigateToCategoryDetail(effect.category)
                }
                CategoryListUiEffect.NavigateToSettings -> onNavigateToSettings()
                CategoryListUiEffect.NavigateToAbout -> onNavigateToAbout()
            }
        }
    }

    CategoryListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    uiState: CategoryListUiState,
    onEvent: (CategoryListUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.category_list_title))
                },
                actions = {
                    IconButton(onClick = { onEvent(CategoryListUiEvent.OnAboutClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.about_title)
                        )
                    }
                    IconButton(onClick = { onEvent(CategoryListUiEvent.OnSettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(CategoryListUiEvent.OnAddCategoryClicked) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.category_add_button)
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.categories,
                    key = { it.id }
                ) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onEvent(CategoryListUiEvent.OnCategoryClicked(category)) }
                    )
                }
            }
        }
    }

    uiState.createDialog?.let { dialogState ->
        CreateCategoryDialog(
            state = dialogState,
            onNameChanged = { onEvent(CategoryListUiEvent.OnCreateNameChanged(it)) },
            onAccessModeChanged = { onEvent(CategoryListUiEvent.OnCreateAccessModeChanged(it)) },
            onPasswordChanged = { onEvent(CategoryListUiEvent.OnCreatePasswordChanged(it)) },
            onConfirm = { onEvent(CategoryListUiEvent.OnConfirmCreateCategory) },
            onDismiss = { onEvent(CategoryListUiEvent.OnDismissCreateDialog) }
        )
    }

    uiState.passwordPrompt?.let { promptState ->
        PasswordPromptDialog(
            state = promptState,
            onPasswordChanged = { onEvent(CategoryListUiEvent.OnPasswordPromptChanged(it)) },
            onConfirm = { onEvent(CategoryListUiEvent.OnConfirmPasswordPrompt) },
            onDismiss = { onEvent(CategoryListUiEvent.OnDismissPasswordPrompt) }
        )
    }
}
