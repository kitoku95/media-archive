package io.github.kitoku95.mediaarchive.feature.categorylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.usecase.category.CreateCategoryUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.category.GetCategoriesUseCase
import io.github.kitoku95.mediaarchive.domain.usecase.category.VerifyCategoryPasswordUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the Category list, creation of `public`/`private` Categories,
 * and session-scoped password verification for `private` Categories.
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val verifyCategoryPasswordUseCase: VerifyCategoryPasswordUseCase
) : ViewModel() {

    private val createDialogState = MutableStateFlow<CreateCategoryDialogState?>(null)
    private val passwordPromptState = MutableStateFlow<PasswordPromptState?>(null)

    val uiState: StateFlow<CategoryListUiState> = combine(
        getCategoriesUseCase(),
        createDialogState,
        passwordPromptState
    ) { categories, createDialog, passwordPrompt ->
        CategoryListUiState(
            categories = categories,
            isLoading = false,
            createDialog = createDialog,
            passwordPrompt = passwordPrompt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryListUiState()
    )

    private val _uiEffect = Channel<CategoryListUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CategoryListUiEffect> = _uiEffect.receiveAsFlow()

    fun onEvent(event: CategoryListUiEvent) {
        when (event) {
            is CategoryListUiEvent.OnCategoryClicked -> handleCategoryClick(event.category)
            CategoryListUiEvent.OnAddCategoryClicked -> {
                createDialogState.value = CreateCategoryDialogState()
            }
            CategoryListUiEvent.OnDismissCreateDialog -> {
                createDialogState.value = null
            }
            is CategoryListUiEvent.OnCreateNameChanged -> {
                createDialogState.value = createDialogState.value?.copy(
                    name = event.name,
                    error = null
                )
            }
            is CategoryListUiEvent.OnCreateAccessModeChanged -> {
                createDialogState.value = createDialogState.value?.copy(
                    accessMode = event.accessMode,
                    password = if (event.accessMode == AccessMode.PUBLIC) "" else createDialogState.value?.password.orEmpty(),
                    error = null
                )
            }
            is CategoryListUiEvent.OnCreatePasswordChanged -> {
                createDialogState.value = createDialogState.value?.copy(
                    password = event.password,
                    error = null
                )
            }
            CategoryListUiEvent.OnConfirmCreateCategory -> submitCreateCategory()
            CategoryListUiEvent.OnDismissPasswordPrompt -> {
                passwordPromptState.value = null
            }
            is CategoryListUiEvent.OnPasswordPromptChanged -> {
                passwordPromptState.value = passwordPromptState.value?.copy(
                    password = event.password,
                    isError = false
                )
            }
            CategoryListUiEvent.OnConfirmPasswordPrompt -> submitPasswordPrompt()
            CategoryListUiEvent.OnSettingsClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(CategoryListUiEffect.NavigateToSettings)
                }
            }
            CategoryListUiEvent.OnAboutClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(CategoryListUiEffect.NavigateToAbout)
                }
            }
        }
    }

    private fun handleCategoryClick(category: Category) {
        when (category.accessMode) {
            AccessMode.PUBLIC -> {
                viewModelScope.launch {
                    _uiEffect.send(CategoryListUiEffect.NavigateToCategoryDetail(category))
                }
            }
            AccessMode.PRIVATE -> {
                passwordPromptState.value = PasswordPromptState(category = category)
            }
        }
    }

    private fun submitCreateCategory() {
        val current = createDialogState.value ?: return
        if (current.isSubmitting) return

        val trimmedName = current.name.trim()
        if (trimmedName.isEmpty()) {
            createDialogState.value = current.copy(error = CreateCategoryError.BLANK_NAME)
            return
        }
        if (current.accessMode == AccessMode.PRIVATE && current.password.isEmpty()) {
            createDialogState.value = current.copy(error = CreateCategoryError.EMPTY_PASSWORD)
            return
        }

        createDialogState.value = current.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            val result = createCategoryUseCase(
                name = trimmedName,
                accessMode = current.accessMode,
                password = if (current.accessMode == AccessMode.PRIVATE) current.password else null
            )
            result.fold(
                onSuccess = {
                    createDialogState.value = null
                },
                onFailure = { throwable ->
                    val mappedError = when {
                        throwable.message?.contains("already exists", ignoreCase = true) == true -> {
                            CreateCategoryError.DUPLICATE_NAME
                        }
                        throwable.message?.contains("blank", ignoreCase = true) == true -> {
                            CreateCategoryError.BLANK_NAME
                        }
                        throwable.message?.contains("password", ignoreCase = true) == true -> {
                            CreateCategoryError.EMPTY_PASSWORD
                        }
                        else -> CreateCategoryError.GENERIC
                    }
                    createDialogState.value = createDialogState.value?.copy(
                        isSubmitting = false,
                        error = mappedError
                    )
                }
            )
        }
    }

    private fun submitPasswordPrompt() {
        val current = passwordPromptState.value ?: return
        if (current.isVerifying) return

        if (current.password.isEmpty()) {
            passwordPromptState.value = current.copy(isError = true)
            return
        }

        passwordPromptState.value = current.copy(isVerifying = true, isError = false)
        viewModelScope.launch {
            val isValid = verifyCategoryPasswordUseCase(
                categoryId = current.category.id,
                password = current.password
            )
            if (isValid) {
                val unlockedCategory = current.category
                passwordPromptState.value = null
                _uiEffect.send(CategoryListUiEffect.NavigateToCategoryDetail(unlockedCategory))
            } else {
                passwordPromptState.value = passwordPromptState.value?.copy(
                    isVerifying = false,
                    isError = true
                )
            }
        }
    }
}
