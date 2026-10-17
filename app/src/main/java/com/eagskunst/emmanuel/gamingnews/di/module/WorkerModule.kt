package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.domain.notification.ReleaseNotifier
import com.eagskunst.emmanuel.gamingnews.core.notification.AppNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Bindings needed by background workers (singleton-scoped). */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    fun provideReleaseNotifier(notifier: AppNotifier): ReleaseNotifier = notifier
}
