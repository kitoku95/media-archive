package io.github.kitoku95.mediaarchive.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

/**
 * Room entity representing a row in the `categories` SQLite table.
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "category_type"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE)
    val name: String,

    @ColumnInfo(name = "category_type", defaultValue = "MEDIA")
    val categoryType: String = CategoryType.MEDIA.name,

    @ColumnInfo(name = "access_mode")
    val accessMode: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String?,

    @ColumnInfo(name = "password_salt")
    val passwordSalt: String?,

    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
