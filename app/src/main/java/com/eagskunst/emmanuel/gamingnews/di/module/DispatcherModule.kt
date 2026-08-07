package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.common.DefaultDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider
}
