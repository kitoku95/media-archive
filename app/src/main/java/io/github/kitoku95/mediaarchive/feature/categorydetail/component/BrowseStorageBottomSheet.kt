package io.github.kitoku95.mediaarchive.feature.categorydetail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

/**
 * Bottom sheet presenting manual SAF storage selection options tailored to [categoryType]:
 * individual files or an entire folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseStorageBottomSheet(
    categoryType: CategoryType,
    onSelectFiles: () -> Unit,
    onSelectFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    val isMedia = categoryType == CategoryType.MEDIA
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.category_detail_browse_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            if (isMedia) {
                                R.string.category_detail_browse_media_files
                            } else {
                                R.string.category_detail_browse_document_files
                            }
                        )
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(
                            if (isMedia) {
                                R.string.category_detail_browse_media_files_desc
                            } else {
                                R.string.category_detail_browse_document_files_desc
                            }
                        )
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = if (isMedia) Icons.Outlined.PermMedia else Icons.Outlined.Description,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable(onClick = onSelectFiles)
            )
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.category_detail_browse_folder))
                },
                supportingContent = {
                    Text(
                        text = stringResource(
                            if (isMedia) {
                                R.string.category_detail_browse_media_folder_desc
                            } else {
                                R.string.category_detail_browse_document_folder_desc
                            }
                        )
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.CreateNewFolder,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable(onClick = onSelectFolder)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
