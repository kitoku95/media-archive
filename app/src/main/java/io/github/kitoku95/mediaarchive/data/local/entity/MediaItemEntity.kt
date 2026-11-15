package io.github.kitoku95.mediaarchive.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a row in the `media_items` SQLite table.
 */
@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["category_id", "media_type", "added_at"],
            name = "index_media_items_category_id_media_type_added_at"
        ),
        Index(
            value = ["category_id", "content_uri"],
            unique = true,
            name = "index_media_items_category_id_content_uri"
        )
    ]
)
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "media_type")
    val mediaType: String,

    @ColumnInfo(name = "content_uri")
    val contentUri: String,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long?,

    @ColumnInfo(name = "size_bytes", defaultValue = "0")
    val sizeBytes: Long = 0L,

    @ColumnInfo(name = "added_at")
    val addedAt: Long
)
