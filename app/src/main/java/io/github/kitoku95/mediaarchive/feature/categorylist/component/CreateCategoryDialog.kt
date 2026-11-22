package io.github.kitoku95.mediaarchive.feature.categorylist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.feature.categorylist.CreateCategoryDialogState
import io.github.kitoku95.mediaarchive.feature.categorylist.CreateCategoryError

/**
 * Dialog for creating a new `public` or `private` Category of type `MEDIA` or `DOCUMENT`.
 *
 * When `Private` is selected, any non-empty password is accepted without
 * minimum length or complexity restrictions (HR-03).
 */
@Composable
fun CreateCategoryDialog(
    state: CreateCategoryDialogState,
    onNameChanged: (String) -> Unit,
    onCategoryTypeChanged: (CategoryType) -> Unit,
    onAccessModeChanged: (AccessMode) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val errorMessage = when (state.error) {
        CreateCategoryError.BLANK_NAME -> stringResource(R.string.category_error_blank_name)
        CreateCategoryError.DUPLICATE_NAME -> stringResource(R.string.category_error_duplicate_name)
        CreateCategoryError.EMPTY_PASSWORD -> stringResource(R.string.category_error_empty_password)
        CreateCategoryError.GENERIC -> stringResource(R.string.category_error_generic)
        null -> null
    }

    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = {
            Text(text = stringResource(R.string.category_create_dialog_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.category_name_label)) },
                    placeholder = { Text(text = stringResource(R.string.category_name_placeholder)) },
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    isError = state.error == CreateCategoryError.BLANK_NAME ||
                        state.error == CreateCategoryError.DUPLICATE_NAME,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (state.accessMode == AccessMode.PRIVATE) {
                            ImeAction.Next
                        } else {
                            ImeAction.Done
                        }
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (state.accessMode == AccessMode.PUBLIC) onConfirm()
                        }
                    )
                )

                Text(
                    text = stringResource(R.string.category_type_label),
                    style = MaterialTheme.typography.labelLarge
                )

                val categoryTypes = listOf(CategoryType.MEDIA, CategoryType.DOCUMENT)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    categoryTypes.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = state.categoryType == type,
                            onClick = { if (!state.isSubmitting) onCategoryTypeChanged(type) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = categoryTypes.size
                            ),
                            label = {
                                Text(
                                    text = stringResource(
                                        if (type == CategoryType.MEDIA) {
                                            R.string.category_type_media
                                        } else {
                                            R.string.category_type_document
                                        }
                                    )
                                )
                            }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.category_access_mode_label),
                    style = MaterialTheme.typography.labelLarge
                )

                val modes = listOf(AccessMode.PUBLIC, AccessMode.PRIVATE)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.accessMode == mode,
                            onClick = { if (!state.isSubmitting) onAccessModeChanged(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = modes.size
                            ),
                            label = {
                                Text(
                                    text = stringResource(
                                        if (mode == AccessMode.PUBLIC) {
                                            R.string.category_mode_public
                                        } else {
                                            R.string.category_mode_private
                                        }
                                    )
                                )
                            }
                        )
                    }
                }

                if (state.accessMode == AccessMode.PRIVATE) {
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.category_password_label)) },
                        placeholder = { Text(text = stringResource(R.string.category_password_placeholder)) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.error == CreateCategoryError.EMPTY_PASSWORD,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = stringResource(
                                        if (passwordVisible) {
                                            R.string.category_password_hide
                                        } else {
                                            R.string.category_password_show
                                        }
                                    )
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onConfirm() }
                        )
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isSubmitting
            ) {
                Text(text = stringResource(R.string.category_create_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSubmitting
            ) {
                Text(text = stringResource(R.string.category_dialog_cancel))
            }
        }
    )
}
