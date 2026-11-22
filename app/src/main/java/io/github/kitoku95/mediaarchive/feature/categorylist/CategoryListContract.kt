package io.github.kitoku95.mediaarchive.feature.categorylist

import androidx.compose.runtime.Immutable
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

@Immutable
data class CategoryListUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val createDialog: CreateCategoryDialogState? = null,
    val passwordPrompt: PasswordPromptState? = null,
    val deleteConfirmationCategory: Category? = null
)

@Immutable
data class CreateCategoryDialogState(
    val name: String = "",
    val categoryType: CategoryType = CategoryType.MEDIA,
    val accessMode: AccessMode = AccessMode.PUBLIC,
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: CreateCategoryError? = null
)

enum class CreateCategoryError {
    BLANK_NAME,
    DUPLICATE_NAME,
    EMPTY_PASSWORD,
    GENERIC
}

@Immutable
data class PasswordPromptState(
    val category: Category,
    val password: String = "",
    val isVerifying: Boolean = false,
    val isError: Boolean = false
)

sealed interface CategoryListUiEvent {
    data class OnCategoryClicked(val category: Category) : CategoryListUiEvent
    data object OnAddCategoryClicked : CategoryListUiEvent
    data object OnDismissCreateDialog : CategoryListUiEvent
    data class OnCreateNameChanged(val name: String) : CategoryListUiEvent
    data class OnCreateCategoryTypeChanged(val categoryType: CategoryType) : CategoryListUiEvent
    data class OnCreateAccessModeChanged(val accessMode: AccessMode) : CategoryListUiEvent
    data class OnCreatePasswordChanged(val password: String) : CategoryListUiEvent
    data object OnConfirmCreateCategory : CategoryListUiEvent
    data object OnDismissPasswordPrompt : CategoryListUiEvent
    data class OnPasswordPromptChanged(val password: String) : CategoryListUiEvent
    data object OnConfirmPasswordPrompt : CategoryListUiEvent
    data class OnDeleteCategoryClicked(val category: Category) : CategoryListUiEvent
    data object OnDismissDeleteCategoryDialog : CategoryListUiEvent
    data object OnConfirmDeleteCategory : CategoryListUiEvent
    data object OnSettingsClicked : CategoryListUiEvent
    data object OnAboutClicked : CategoryListUiEvent
}

sealed interface CategoryListUiEffect {
    data class NavigateToCategoryDetail(val category: Category) : CategoryListUiEffect
    data object NavigateToSettings : CategoryListUiEffect
    data object NavigateToAbout : CategoryListUiEffect
    data object ShowDeleteCategoryError : CategoryListUiEffect
}
