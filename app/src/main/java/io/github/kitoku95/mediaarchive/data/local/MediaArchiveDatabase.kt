package io.github.kitoku95.mediaarchive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.kitoku95.mediaarchive.data.local.dao.CategoryDao
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity

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
         * Room callback that seeds the non-deletable `Main` (`PUBLIC`) Category
         * immediately when the SQLite database is first created.
         */
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (
                        name, access_mode, password_hash, password_salt, is_default, created_at
                    ) VALUES (?, ?, NULL, NULL, 1, ?)
                    """.trimIndent(),
                    arrayOf<Any>(
                        DEFAULT_CATEGORY_NAME,
                        DEFAULT_ACCESS_MODE,
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
