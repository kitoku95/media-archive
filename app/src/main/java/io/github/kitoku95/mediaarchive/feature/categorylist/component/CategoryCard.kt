package io.github.kitoku95.mediaarchive.feature.categorylist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.kitoku95.mediaarchive.R
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.Category
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

/**
 * Displays a single Category item in a uniform `72.dp` height 4-column layout:
 * 1. Private lock icon (hidden when public, preserving column alignment)
 * 2. Category type icon (`Folder` for Media, `Description` for Document)
 * 3. Category name (widest column, single-line ellipsis)
 * 4. Delete button (visible for user-created categories, hidden for default `Main` categories)
 */
@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPrivate = category.accessMode == AccessMode.PRIVATE
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Column 1: Private lock indicator (hidden when public)
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPrivate) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.category_mode_private),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Column 2: Category type icon (Folder for Media, Document for Document)
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (category.categoryType) {
                        CategoryType.MEDIA -> Icons.Outlined.Folder
                        CategoryType.DOCUMENT -> Icons.Outlined.Description
                    },
                    contentDescription = stringResource(
                        when (category.categoryType) {
                            CategoryType.MEDIA -> R.string.category_type_media
                            CategoryType.DOCUMENT -> R.string.category_type_document
                        }
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Column 3: Category name (widest column)
            Text(
                text = if (category.isDefault) {
                    stringResource(R.string.category_default_name)
                } else {
                    category.name
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Column 4: Delete action (hidden for default Main categories)
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!category.isDefault && onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.category_delete_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
