package io.github.kitoku95.mediaarchive.feature.categorydetail.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import java.util.Locale

/**
 * Uniform `72.dp` height document row with a color-coded `44.dp` extension badge
 * and single-line ellipsis truncation for long filenames.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentListItem(
    item: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extension = resolveExtensionLabel(item.displayName, item.mimeType)
    val badgeColor = badgeColorForExtension(extension)
    val formattedSize = formatFileSize(item.sizeBytes)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = extension.take(4),
                    color = Color.White,
                    fontSize = if (extension.length >= 4) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (formattedSize.isNotEmpty()) "$extension • $formattedSize" else extension,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun resolveExtensionLabel(displayName: String, mimeType: String): String {
    val fromName = displayName.substringAfterLast('.', "").trim().uppercase(Locale.US)
    if (fromName.isNotEmpty() && fromName.length <= 5) {
        return fromName
    }
    return when {
        mimeType.contains("pdf", ignoreCase = true) -> "PDF"
        mimeType.contains("wordprocessingml", ignoreCase = true) -> "DOCX"
        mimeType.contains("msword", ignoreCase = true) -> "DOC"
        mimeType.contains("spreadsheetml", ignoreCase = true) -> "XLSX"
        mimeType.contains("ms-excel", ignoreCase = true) -> "XLS"
        mimeType.contains("presentationml", ignoreCase = true) -> "PPTX"
        mimeType.contains("ms-powerpoint", ignoreCase = true) -> "PPT"
        mimeType.startsWith("text/", ignoreCase = true) -> "TXT"
        else -> "DOC"
    }
}

private fun badgeColorForExtension(ext: String): Color = when (ext) {
    "PDF" -> Color(0xFFC62828)
    "DOC", "DOCX", "ODT", "RTF" -> Color(0xFF1565C0)
    "XLS", "XLSX", "ODS", "CSV" -> Color(0xFF2E7D32)
    "PPT", "PPTX", "ODP" -> Color(0xFFD84315)
    "TXT", "MD", "JSON", "XML" -> Color(0xFF546E7A)
    else -> Color(0xFF5D4037)
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return ""
    val kb = sizeBytes / 1024.0
    if (kb < 1024.0) {
        return String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
    }
    val mb = kb / 1024.0
    if (mb < 1024.0) {
        return String.format(Locale.US, "%.1f MB", mb)
    }
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}
