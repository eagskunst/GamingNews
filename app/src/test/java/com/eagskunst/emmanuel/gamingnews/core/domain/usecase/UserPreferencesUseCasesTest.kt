package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesUseCasesTest {

    private val fakeRepository = FakeUserPreferencesRepository()

    @Test
    fun `when updateThemeModeUseCase is invoked then theme mode is updated and other preferences are untouched`() = runTest {
        val useCase = UpdateThemeModeUseCase(fakeRepository)

        useCase(ThemeMode.DARK)

        val preferences = fakeRepository.userPreferences.first()
        assertEquals(ThemeMode.DARK, preferences.themeMode)
        assertEquals(true, preferences.dynamicColor)
        assertEquals(true, preferences.loadImages)
        assertEquals(false, preferences.dailyReminder)
        assertEquals(9, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.READER_MODE, preferences.articleOpenMode)
    }

    @Test
    fun `when updateDynamicColorUseCase is invoked then dynamic color is updated and other preferences are untouched`() = runTest {
        val useCase = UpdateDynamicColorUseCase(fakeRepository)

        useCase(false)

        val preferences = fakeRepository.userPreferences.first()
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertEquals(false, preferences.dynamicColor)
        assertEquals(true, preferences.loadImages)
        assertEquals(false, preferences.dailyReminder)
        assertEquals(9, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.READER_MODE, preferences.articleOpenMode)
    }

    @Test
    fun `when updateDarkThemeUseCase is invoked then theme mode is set to DARK or LIGHT`() = runTest {
        val useCase = UpdateDarkThemeUseCase(fakeRepository)

        useCase(true)
        assertEquals(ThemeMode.DARK, fakeRepository.userPreferences.first().themeMode)

        useCase(false)
        assertEquals(ThemeMode.LIGHT, fakeRepository.userPreferences.first().themeMode)
    }

    @Test
    fun `when updateLoadImagesUseCase is invoked then load images is updated and other preferences are untouched`() = runTest {
        val useCase = UpdateLoadImagesUseCase(fakeRepository)

        useCase(false)

        val preferences = fakeRepository.userPreferences.first()
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertEquals(true, preferences.dynamicColor)
        assertEquals(false, preferences.loadImages)
        assertEquals(false, preferences.dailyReminder)
        assertEquals(9, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.READER_MODE, preferences.articleOpenMode)
    }

    @Test
    fun `when updateDailyReminderUseCase is invoked then daily reminder is updated and other preferences are untouched`() = runTest {
        val useCase = UpdateDailyReminderUseCase(fakeRepository)

        useCase(true)

        val preferences = fakeRepository.userPreferences.first()
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertEquals(true, preferences.dynamicColor)
        assertEquals(true, preferences.loadImages)
        assertEquals(true, preferences.dailyReminder)
        assertEquals(9, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.READER_MODE, preferences.articleOpenMode)
    }

    @Test
    fun `when updateDailyReminderHourUseCase is invoked then daily reminder hour is updated and other preferences are untouched`() = runTest {
        val useCase = UpdateDailyReminderHourUseCase(fakeRepository)

        useCase(21)

        val preferences = fakeRepository.userPreferences.first()
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertEquals(true, preferences.dynamicColor)
        assertEquals(true, preferences.loadImages)
        assertEquals(false, preferences.dailyReminder)
        assertEquals(21, preferences.dailyReminderHour)
        assertEquals(ArticleOpenMode.READER_MODE, preferences.articleOpenMode)
    }

    @Test
    fun `when getUserPreferencesUseCase is invoked then it returns current preferences value`() = runTest {
        fakeRepository.preferencesFlow.value = Fixtures.userPreferences(
            themeMode = ThemeMode.DARK,
            dynamicColor = false,
            articleOpenMode = ArticleOpenMode.EXTERNAL_BROWSER
        )

        val useCase = GetUserPreferencesUseCase(fakeRepository)

        assertEquals(
            Fixtures.userPreferences(
                themeMode = ThemeMode.DARK,
                dynamicColor = false,
                articleOpenMode = ArticleOpenMode.EXTERNAL_BROWSER
            ),
            useCase().first()
        )
    }
}
