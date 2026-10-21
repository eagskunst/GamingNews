package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.domain.notification.ReleaseNotifier
import com.eagskunst.emmanuel.gamingnews.core.notification.AppNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

/** Bindings needed by background workers (singleton-scoped). */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    fun provideReleaseNotifier(notifier: AppNotifier): ReleaseNotifier = notifier

    /** Wall clock for the release sync; a bound lambda so tests can pin the date. */
    @Provides
    fun provideLocalDateClock(): () -> LocalDate = LocalDate::now
}
