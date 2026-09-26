package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelection
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionReconciler
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Reusable [UserPreferencesRepository] fake backed by a [MutableStateFlow], shared across all
 * use case/ViewModel/Compose UI tests that depend on user preferences.
 *
 * [storedPlatformIds] mirrors the raw persisted string set: reads are reconciled through
 * [PlatformSelectionReconciler] exactly like the real repository, and writes replace the
 * stored set (after reconciliation), so retired/unknown/malformed handling is exercisable.
 * Set [failPlatformWrites] to simulate a persistence failure.
 */
class FakeUserPreferencesRepository(
    initial: UserPreferences = Fixtures.userPreferences(),
    private val catalog: PlatformCatalog? = null
) : UserPreferencesRepository {

    val preferencesFlow = MutableStateFlow(initial)
    val storedPlatformIds = MutableStateFlow<Set<String>>(emptySet())

    var failPlatformWrites = false

    override val userPreferences: Flow<UserPreferences> = preferencesFlow

    override val releasePlatformSelection: Flow<PlatformSelection> =
        storedPlatformIds.map { stored ->
            val reconciled = catalog?.let {
                PlatformSelectionReconciler.reconcile(stored, it)
            }
            if (reconciled != null && reconciled.requiresPersistence) {
                storedPlatformIds.value = reconciled.storedValue
            }
            reconciled?.selection ?: PlatformSelection(
                selectedIds = stored.mapNotNull { it.toIntOrNull() }.toSet()
            )
        }

    override suspend fun updateReleasePlatformIds(ids: Set<Int>) {
        if (failPlatformWrites) throw IllegalStateException("platform selection write failed")
        storedPlatformIds.value = ids.mapTo(mutableSetOf()) { it.toString() }
    }

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

    override suspend fun updateDailyReminderHour(hour: Int) {
        preferencesFlow.update { it.copy(dailyReminderHour = hour) }
    }

    override suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
        preferencesFlow.update { it.copy(articleOpenMode = mode) }
    }

    override suspend fun updateApplyGlobalMuteRulesToReviews(enabled: Boolean) {
        preferencesFlow.update { it.copy(applyGlobalMuteRulesToReviews = enabled) }
    }
}
