package io.github.kitoku95.mediaarchive.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.kitoku95.mediaarchive.data.local.MediaArchiveDatabase
import io.github.kitoku95.mediaarchive.data.local.dao.CategoryDao
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMediaArchiveDatabase(
        @ApplicationContext context: Context
    ): MediaArchiveDatabase {
        return Room.databaseBuilder(
            context,
            MediaArchiveDatabase::class.java,
            MediaArchiveDatabase.DATABASE_NAME
        )
            .addCallback(MediaArchiveDatabase.SEED_CALLBACK)
            .build()
    }

    @Provides
    fun provideCategoryDao(database: MediaArchiveDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideMediaItemDao(database: MediaArchiveDatabase): MediaItemDao {
        return database.mediaItemDao()
    }
}
