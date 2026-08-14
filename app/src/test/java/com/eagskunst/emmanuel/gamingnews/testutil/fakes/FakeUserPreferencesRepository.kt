package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Reusable [UserPreferencesRepository] fake backed by a [MutableStateFlow], shared across all
 * use case/ViewModel/Compose UI tests that depend on user preferences.
 */
class FakeUserPreferencesRepository(
    initial: UserPreferences = Fixtures.userPreferences()
) : UserPreferencesRepository {

    val preferencesFlow = MutableStateFlow(initial)

    override val userPreferences: Flow<UserPreferences> = preferencesFlow

    override suspend fun updateThemeMode(mode: ThemeMode) {
        preferencesFlow.update { it.copy(themeMode = mode) }
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        preferencesFlow.update { it.copy(dynamicColor = enabled) }
    }

    override suspend fun updateDarkTheme(enabled: Boolean) {
        updateThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    override suspend fun updateLoadImages(enabled: Boolean) {
        preferencesFlow.update { it.copy(loadImages = enabled) }
    }

    override suspend fun updateDailyReminder(enabled: Boolean) {
        preferencesFlow.update { it.copy(dailyReminder = enabled) }
    }

    override suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
        preferencesFlow.update { it.copy(articleOpenMode = mode) }
    }
}

