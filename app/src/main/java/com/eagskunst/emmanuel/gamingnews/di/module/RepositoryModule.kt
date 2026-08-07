package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.data.repository.DefaultNewsRepository
import com.eagskunst.emmanuel.gamingnews.core.data.repository.DefaultReleasesRepository
import com.eagskunst.emmanuel.gamingnews.core.data.repository.DefaultTopicsRepository
import com.eagskunst.emmanuel.gamingnews.core.data.repository.DefaultUserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        impl: DefaultNewsRepository
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindReleasesRepository(
        impl: DefaultReleasesRepository
    ): ReleasesRepository

    @Binds
    @Singleton
    abstract fun bindTopicsRepository(
        impl: DefaultTopicsRepository
    ): TopicsRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DefaultUserPreferencesRepository
    ): UserPreferencesRepository
}
