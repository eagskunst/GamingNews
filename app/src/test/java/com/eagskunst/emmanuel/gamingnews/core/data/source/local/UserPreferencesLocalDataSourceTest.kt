package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

// The Context.userPreferencesDataStore delegate caches a single DataStore instance for the
// whole test class run, so preferences persist across test methods; reset to defaults before
// each test rather than relying on a pristine on-disk store.
@RunWith(RobolectricTestRunner::class)
class UserPreferencesLocalDataSourceTest {

    private val dataSource = UserPreferencesLocalDataSource(RuntimeEnvironment.getApplication())

    @Before
    fun resetToDefaults() = runTest {
        dataSource.updateThemeMode(ThemeMode.SYSTEM)
        dataSource.updateDynamicColor(true)
        dataSource.updateLoadImages(true)
        dataSource.updateDailyReminder(false)
        dataSource.updateDailyReminderHour(9)
        dataSource.updateArticleOpenMode(ArticleOpenMode.EXTERNAL_BROWSER)
    }

    @Test
    fun `given nothing stored when userPreferences then returns defaults`() = runTest {
        val preferences = dataSource.userPreferences.first()

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertEquals(true, preferences.dynamicColor)
        assertEquals(true, preferences.loadImages)
        assertEquals(false, preferences.dailyReminder)
        assertEquals(9, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.EXTERNAL_BROWSER, preferences.articleOpenMode)
    }

    @Test
    fun `given updateThemeMode when userPreferences then reflects new value`() = runTest {
        dataSource.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, dataSource.userPreferences.first().themeMode)
    }

    @Test
    fun `given updateDynamicColor when userPreferences then reflects new value`() = runTest {
        dataSource.updateDynamicColor(false)

        assertEquals(false, dataSource.userPreferences.first().dynamicColor)
    }

    @Test
    fun `given updateDarkTheme when userPreferences then reflects new value`() = runTest {
        dataSource.updateDarkTheme(true)

        assertEquals(ThemeMode.DARK, dataSource.userPreferences.first().themeMode)
    }

    @Test
    fun `given updateLoadImages when userPreferences then reflects new value`() = runTest {
        dataSource.updateLoadImages(false)

        assertEquals(false, dataSource.userPreferences.first().loadImages)
    }

    @Test
    fun `given updateDailyReminder when userPreferences then reflects new value`() = runTest {
        dataSource.updateDailyReminder(true)

        assertEquals(true, dataSource.userPreferences.first().dailyReminder)
    }

    @Test
    fun `given updateDailyReminderHour when userPreferences then reflects new value`() = runTest {
        dataSource.updateDailyReminderHour(21)

        assertEquals(21, dataSource.userPreferences.first().dailyReminderHour)
    }

    @Test
    fun `given updateArticleOpenMode when userPreferences then reflects new value`() = runTest {
        dataSource.updateArticleOpenMode(ArticleOpenMode.READER_MODE)

        assertEquals(ArticleOpenMode.READER_MODE, dataSource.userPreferences.first().articleOpenMode)
    }
}
