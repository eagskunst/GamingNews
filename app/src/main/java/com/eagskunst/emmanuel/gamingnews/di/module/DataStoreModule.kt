package com.eagskunst.emmanuel.gamingnews.di.module

import android.content.Context
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.TopicsLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.UserPreferencesLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesLocalDataSource(
        @ApplicationContext context: Context
    ): UserPreferencesLocalDataSource = UserPreferencesLocalDataSource(context)

    @Provides
    @Singleton
    fun provideTopicsLocalDataSource(
        @ApplicationContext context: Context
    ): TopicsLocalDataSource = TopicsLocalDataSource(context)

    @Provides
    @Singleton
    fun provideIgdbAuthLocalDataSource(
        @ApplicationContext context: Context
    ): IgdbAuthLocalDataSource = IgdbAuthLocalDataSource(context)
}
