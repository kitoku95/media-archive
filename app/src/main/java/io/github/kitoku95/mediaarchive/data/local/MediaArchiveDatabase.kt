package io.github.kitoku95.mediaarchive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 3,
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

        /**
         * Migrates v1 (`categories` + `media_items`) to v2 (`archive_folders` + folder/canonical columns).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS archive_folders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category_id INTEGER NOT NULL,
                        parent_folder_id INTEGER DEFAULT NULL,
                        name TEXT NOT NULL,
                        tree_uri TEXT NOT NULL,
                        canonical_id TEXT NOT NULL,
                        added_at INTEGER NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                        FOREIGN KEY(parent_folder_id) REFERENCES archive_folders(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_archive_folders_category_parent_name
                    ON archive_folders (category_id, parent_folder_id, name)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_archive_folders_parent_folder_id
                    ON archive_folders (parent_folder_id)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_archive_folders_category_canonical_id
                    ON archive_folders (category_id, canonical_id)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS media_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category_id INTEGER NOT NULL,
                        folder_id INTEGER DEFAULT NULL,
                        media_type TEXT NOT NULL,
                        content_uri TEXT NOT NULL,
                        canonical_id TEXT NOT NULL,
                        thumbnail_path TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        mime_type TEXT NOT NULL,
                        duration_ms INTEGER,
                        size_bytes INTEGER NOT NULL DEFAULT 0,
                        added_at INTEGER NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                        FOREIGN KEY(folder_id) REFERENCES archive_folders(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO media_items_new (
                        id, category_id, folder_id, media_type, content_uri, canonical_id,
                        thumbnail_path, display_name, mime_type, duration_ms, size_bytes, added_at
                    )
                    SELECT
                        id, category_id, NULL, media_type, content_uri, content_uri,
                        thumbnail_path, display_name, mime_type, duration_ms, size_bytes, added_at
                    FROM media_items
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE media_items")
                db.execSQL("ALTER TABLE media_items_new RENAME TO media_items")

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_media_items_category_folder_type_added_at
                    ON media_items (category_id, folder_id, media_type, added_at)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_media_items_folder_id
                    ON media_items (folder_id)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_media_items_category_canonical_id
                    ON media_items (category_id, canonical_id)
                    """.trimIndent()
                )
            }
        }

        /**
         * Migrates v2 to v3 by flattening all media items directly under their Category
         * (removing `folder_id` and dropping `archive_folders`) while preserving `canonical_id`.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS media_items_v3 (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category_id INTEGER NOT NULL,
                        media_type TEXT NOT NULL,
                        content_uri TEXT NOT NULL,
                        canonical_id TEXT NOT NULL,
                        thumbnail_path TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        mime_type TEXT NOT NULL,
                        duration_ms INTEGER,
                        size_bytes INTEGER NOT NULL DEFAULT 0,
                        added_at INTEGER NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO media_items_v3 (
                        id, category_id, media_type, content_uri, canonical_id,
                        thumbnail_path, display_name, mime_type, duration_ms, size_bytes, added_at
                    )
                    SELECT
                        id, category_id, media_type, content_uri, canonical_id,
                        thumbnail_path, display_name, mime_type, duration_ms, size_bytes, added_at
                    FROM media_items
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE media_items")
                db.execSQL("ALTER TABLE media_items_v3 RENAME TO media_items")
                db.execSQL("DROP TABLE IF EXISTS archive_folders")

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_media_items_category_id_media_type_added_at
                    ON media_items (category_id, media_type, added_at)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_media_items_category_canonical_id
                    ON media_items (category_id, canonical_id)
                    """.trimIndent()
                )
            }
        }
    }
}
