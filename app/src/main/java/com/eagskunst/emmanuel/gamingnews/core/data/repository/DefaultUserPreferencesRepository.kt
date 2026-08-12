package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.UserPreferencesLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultUserPreferencesRepository @Inject constructor(
    private val localDataSource: UserPreferencesLocalDataSource
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = localDataSource.userPreferences

    override suspend fun updateDarkTheme(enabled: Boolean) {
        localDataSource.updateDarkTheme(enabled)
    }

    override suspend fun updateLoadImages(enabled: Boolean) {
        localDataSource.updateLoadImages(enabled)
    }

    override suspend fun updateDailyReminder(enabled: Boolean) {
        localDataSource.updateDailyReminder(enabled)
    }

    override suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
        localDataSource.updateArticleOpenMode(mode)
    }
}
