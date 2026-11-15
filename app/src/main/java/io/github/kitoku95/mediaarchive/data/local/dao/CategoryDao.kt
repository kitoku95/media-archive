package io.github.kitoku95.mediaarchive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `categories` table.
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY is_default DESC, created_at ASC, id ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE is_default = 1)")
    suspend fun hasDefaultCategory(): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(entity: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultCategoryIfMissing(entity: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE id = :id AND is_default = 0")
    suspend fun deleteCustomCategoryById(id: Long): Int
}
