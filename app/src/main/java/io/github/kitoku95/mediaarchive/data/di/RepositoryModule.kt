package io.github.kitoku95.mediaarchive.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.kitoku95.mediaarchive.data.repository.CategoryRepositoryImpl
import io.github.kitoku95.mediaarchive.data.repository.MediaRepositoryImpl
import io.github.kitoku95.mediaarchive.data.repository.UserPreferencesRepositoryImpl
import io.github.kitoku95.mediaarchive.domain.repository.CategoryRepository
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import io.github.kitoku95.mediaarchive.domain.repository.UserPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository
}
