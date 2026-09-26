package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.UserPreferencesLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelection
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionReconciler
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultUserPreferencesRepository @Inject constructor(
    private val localDataSource: UserPreferencesLocalDataSource,
    private val platformCatalog: PlatformCatalog
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = localDataSource.userPreferences

    override val releasePlatformSelection: Flow<PlatformSelection> =
        localDataSource.releasePlatformIdStrings.map { stored ->
            val result = PlatformSelectionReconciler.reconcile(stored, platformCatalog)
            if (result.requiresPersistence) {
                // Only the Releases-tab selection key is rewritten; other preferences are
                // untouched. A failed write propagates to the collector instead of being
                // silently swallowed.
                localDataSource.updateReleasePlatformIdStrings(result.storedValue)
            }
            result.selection
        }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        localDataSource.updateThemeMode(mode)
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        localDataSource.updateDynamicColor(enabled)
    }

    override suspend fun updateDarkTheme(enabled: Boolean) {
        localDataSource.updateDarkTheme(enabled)
    }

    override suspend fun updateLoadImages(enabled: Boolean) {
        localDataSource.updateLoadImages(enabled)
    }

    override suspend fun updateDailyReminder(enabled: Boolean) {
        localDataSource.updateDailyReminder(enabled)
    }

    override suspend fun updateDailyReminderHour(hour: Int) {
        localDataSource.updateDailyReminderHour(hour)
    }

    override suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
        localDataSource.updateArticleOpenMode(mode)
    }

    override suspend fun updateApplyGlobalMuteRulesToReviews(enabled: Boolean) {
        localDataSource.updateApplyGlobalMuteRulesToReviews(enabled)
    }

    override suspend fun updateReleasePlatformIds(ids: Set<Int>) {
        localDataSource.updateReleasePlatformIdStrings(ids.mapTo(mutableSetOf()) { it.toString() })
    }
}
