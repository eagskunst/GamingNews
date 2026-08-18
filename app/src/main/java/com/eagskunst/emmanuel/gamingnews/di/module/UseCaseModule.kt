package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetNewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.OpenArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderHourUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDynamicColorUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateThemeModeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetNewsUseCase(repository: NewsRepository): GetNewsUseCase = GetNewsUseCase(repository)


    @Provides
    fun provideGetSavedArticlesUseCase(repository: NewsRepository): GetSavedArticlesUseCase =
        GetSavedArticlesUseCase(repository)

    @Provides
    fun provideToggleSavedArticleUseCase(repository: NewsRepository): ToggleSavedArticleUseCase =
        ToggleSavedArticleUseCase(repository)

    @Provides
    fun provideGetReleasesUseCase(repository: ReleasesRepository): GetReleasesUseCase =
        GetReleasesUseCase(repository)

    @Provides
    fun provideGetTopicsUseCase(repository: TopicsRepository): GetTopicsUseCase =
        GetTopicsUseCase(repository)

    @Provides
    fun provideAddTopicUseCase(repository: TopicsRepository): AddTopicUseCase =
        AddTopicUseCase(repository)

    @Provides
    fun provideRemoveTopicUseCase(repository: TopicsRepository): RemoveTopicUseCase =
        RemoveTopicUseCase(repository)

    @Provides
    fun provideGetUserPreferencesUseCase(repository: UserPreferencesRepository): GetUserPreferencesUseCase =
        GetUserPreferencesUseCase(repository)

    @Provides
    fun provideGetArticleOpenModeUseCase(repository: UserPreferencesRepository): GetArticleOpenModeUseCase =
        GetArticleOpenModeUseCase(repository)

    @Provides
    fun provideUpdateArticleOpenModeUseCase(repository: UserPreferencesRepository): UpdateArticleOpenModeUseCase =
        UpdateArticleOpenModeUseCase(repository)

    @Provides
    fun provideUpdateDarkThemeUseCase(repository: UserPreferencesRepository): UpdateDarkThemeUseCase =
        UpdateDarkThemeUseCase(repository)

    @Provides
    fun provideUpdateThemeModeUseCase(repository: UserPreferencesRepository): UpdateThemeModeUseCase =
        UpdateThemeModeUseCase(repository)

    @Provides
    fun provideUpdateDynamicColorUseCase(repository: UserPreferencesRepository): UpdateDynamicColorUseCase =
        UpdateDynamicColorUseCase(repository)

    @Provides
    fun provideUpdateLoadImagesUseCase(repository: UserPreferencesRepository): UpdateLoadImagesUseCase =
        UpdateLoadImagesUseCase(repository)

    @Provides
    fun provideUpdateDailyReminderUseCase(repository: UserPreferencesRepository): UpdateDailyReminderUseCase =
        UpdateDailyReminderUseCase(repository)

    @Provides
    fun provideUpdateDailyReminderHourUseCase(repository: UserPreferencesRepository): UpdateDailyReminderHourUseCase =
        UpdateDailyReminderHourUseCase(repository)
}

