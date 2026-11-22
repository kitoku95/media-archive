package io.github.kitoku95.mediaarchive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.kitoku95.mediaarchive.data.local.dao.CategoryDao
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.domain.model.CategoryType

/**
 * Room SQLite database for MediaArchive, storing Categories and MediaItems.
 */
@Database(
    entities = [
        CategoryEntity::class,
        MediaItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediaArchiveDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun mediaItemDao(): MediaItemDao

    companion object {
        const val DATABASE_NAME = "media_archive.db"
        const val DEFAULT_CATEGORY_NAME = "Main"
        const val DEFAULT_ACCESS_MODE = "PUBLIC"

        /**
         * Room callback that seeds the two non-deletable default `Main` (`PUBLIC`) Categories
         * (one for Media and one for Documents) immediately when the SQLite database is created.
         */
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (
                        name, category_type, access_mode, password_hash, password_salt, is_default, created_at
                    ) VALUES (?, ?, ?, NULL, NULL, 1, ?)
                    """.trimIndent(),
                    arrayOf<Any>(
                        DEFAULT_CATEGORY_NAME,
                        CategoryType.MEDIA.name,
                        DEFAULT_ACCESS_MODE,
                        now
                    )
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (
                        name, category_type, access_mode, password_hash, password_salt, is_default, created_at
                    ) VALUES (?, ?, ?, NULL, NULL, 1, ?)
                    """.trimIndent(),
                    arrayOf<Any>(
                        DEFAULT_CATEGORY_NAME,
                        CategoryType.DOCUMENT.name,
                        DEFAULT_ACCESS_MODE,
                        now + 1L
                    )
                )
            }
        }
    }
}
