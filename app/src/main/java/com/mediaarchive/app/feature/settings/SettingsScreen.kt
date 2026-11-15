package com.mediaarchive.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediaarchive.app.R
import com.mediaarchive.app.core.designsystem.swatchColor
import com.mediaarchive.app.domain.model.AppDarkMode
import com.mediaarchive.app.domain.model.AppLanguage
import com.mediaarchive.app.domain.model.AppThemePalette

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateToAbout -> onNavigateToAbout()
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkPreview = when (uiState.selectedDarkMode) {
        AppDarkMode.SYSTEM -> isSystemInDarkTheme()
        AppDarkMode.LIGHT -> false
        AppDarkMode.DARK -> true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Color Theme Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                AppThemePalette.entries.forEach { palette ->
                    ThemePaletteOptionCard(
                        palette = palette,
                        isSelected = uiState.selectedPalette == palette,
                        isDarkPreview = isDarkPreview,
                        onClick = { onEvent(SettingsUiEvent.OnPaletteSelected(palette)) }
                    )
                }
            }

            // Appearance Mode Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_appearance_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppDarkMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AppDarkMode.entries.size
                            ),
                            selected = uiState.selectedDarkMode == mode,
                            onClick = { onEvent(SettingsUiEvent.OnDarkModeSelected(mode)) },
                            label = {
                                Text(text = darkModeLabel(mode))
                            }
                        )
                    }
                }
            }

            // Language Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEach { language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(SettingsUiEvent.OnLanguageSelected(language))
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = languageLabel(language),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                RadioButton(
                                    selected = uiState.selectedLanguage == language,
                                    onClick = {
                                        onEvent(SettingsUiEvent.OnLanguageSelected(language))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // About Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_about_section_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(SettingsUiEvent.OnAboutClicked) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.settings_about_item),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.settings_about_item_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemePaletteOptionCard(
    palette: AppThemePalette,
    isSelected: Boolean,
    isDarkPreview: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(palette.swatchColor(isDark = isDarkPreview)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paletteTitle(palette),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = paletteDescription(palette),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun paletteTitle(palette: AppThemePalette): String = when (palette) {
    AppThemePalette.NEUTRAL_TAUPE -> stringResource(R.string.settings_theme_neutral_taupe)
    AppThemePalette.DEEP_CRIMSON -> stringResource(R.string.settings_theme_deep_crimson)
    AppThemePalette.DEEP_FOREST -> stringResource(R.string.settings_theme_deep_forest)
    AppThemePalette.DEEP_SAPPHIRE -> stringResource(R.string.settings_theme_deep_sapphire)
}

@Composable
private fun paletteDescription(palette: AppThemePalette): String = when (palette) {
    AppThemePalette.NEUTRAL_TAUPE -> stringResource(R.string.settings_theme_neutral_taupe_desc)
    AppThemePalette.DEEP_CRIMSON -> stringResource(R.string.settings_theme_deep_crimson_desc)
    AppThemePalette.DEEP_FOREST -> stringResource(R.string.settings_theme_deep_forest_desc)
    AppThemePalette.DEEP_SAPPHIRE -> stringResource(R.string.settings_theme_deep_sapphire_desc)
}

@Composable
private fun darkModeLabel(mode: AppDarkMode): String = when (mode) {
    AppDarkMode.SYSTEM -> stringResource(R.string.settings_appearance_system)
    AppDarkMode.LIGHT -> stringResource(R.string.settings_appearance_light)
    AppDarkMode.DARK -> stringResource(R.string.settings_appearance_dark)
}

@Composable
private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
    AppLanguage.VIETNAMESE -> stringResource(R.string.settings_language_vietnamese)
    AppLanguage.JAPANESE -> stringResource(R.string.settings_language_japanese)
}
