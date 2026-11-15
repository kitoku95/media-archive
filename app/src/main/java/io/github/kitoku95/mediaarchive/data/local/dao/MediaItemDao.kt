package io.github.kitoku95.mediaarchive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `media_items` table.
 */
@Dao
interface MediaItemDao {

    @Query(
        """
        SELECT * FROM media_items
        WHERE category_id = :categoryId AND media_type = :mediaType
        ORDER BY added_at DESC, id DESC
        """
    )
    fun observeMediaByCategoryAndType(
        categoryId: Long,
        mediaType: String
    ): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaItemById(id: Long): MediaItemEntity?

    @Query("SELECT thumbnail_path FROM media_items WHERE category_id = :categoryId")
    suspend fun getThumbnailPathsByCategory(categoryId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaItems(items: List<MediaItemEntity>): List<Long>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM media_items
            WHERE category_id = :categoryId AND content_uri = :contentUri
        )
        """
    )
    suspend fun existsInCategory(categoryId: Long, contentUri: String): Boolean

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaItemById(id: Long): Int
}
