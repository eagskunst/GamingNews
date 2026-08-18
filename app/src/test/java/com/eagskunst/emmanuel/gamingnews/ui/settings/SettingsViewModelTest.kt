package com.eagskunst.emmanuel.gamingnews.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderHourUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDynamicColorUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateThemeModeUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeTopicsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    private val fakeTopicsRepository = FakeTopicsRepository()
    private val context: Context = mockk(relaxed = true)

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        context = context,
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
        getTopicsUseCase = GetTopicsUseCase(fakeTopicsRepository),
        updateThemeModeUseCase = UpdateThemeModeUseCase(fakeUserPreferencesRepository),
        updateDynamicColorUseCase = UpdateDynamicColorUseCase(fakeUserPreferencesRepository),
        updateDarkThemeUseCase = UpdateDarkThemeUseCase(fakeUserPreferencesRepository),
        updateLoadImagesUseCase = UpdateLoadImagesUseCase(fakeUserPreferencesRepository),
        updateDailyReminderUseCase = UpdateDailyReminderUseCase(fakeUserPreferencesRepository),
        updateDailyReminderHourUseCase = UpdateDailyReminderHourUseCase(fakeUserPreferencesRepository),
        updateArticleOpenModeUseCase = UpdateArticleOpenModeUseCase(fakeUserPreferencesRepository),
        addTopicUseCase = AddTopicUseCase(fakeTopicsRepository),
        removeTopicUseCase = RemoveTopicUseCase(fakeTopicsRepository)
    )

    @Test
    fun `given preferences and topics emit when uiState is collected then isLoading becomes false and values combine`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(
            themeMode = ThemeMode.DARK,
            dynamicColor = false,
            dailyReminderHour = 21
        )
        fakeTopicsRepository.topicsFlow.value = listOf(Fixtures.topic("RPG"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(ThemeMode.DARK, state.themeMode)
            assertFalse(state.dynamicColor)
            assertTrue(state.darkTheme)
            assertEquals(21, state.dailyReminderHour)
            assertTrue(state.nextReminderLabel.contains("21:00"))
            assertEquals(listOf(Fixtures.topic("RPG")), state.topics)
        }
    }

    @Test
    fun `given mode when setThemeMode is called then repository is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, fakeUserPreferencesRepository.preferencesFlow.value.themeMode)
    }

    @Test
    fun `given enabled when toggleDynamicColor is called then repository is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleDynamicColor(false)

        assertFalse(fakeUserPreferencesRepository.preferencesFlow.value.dynamicColor)
    }

    @Test
    fun `given enabled when toggleDarkTheme is called then repository is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleDarkTheme(true)

        assertEquals(ThemeMode.DARK, fakeUserPreferencesRepository.preferencesFlow.value.themeMode)
    }

    @Test
    fun `given enabled when toggleLoadImages is called then repository is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleLoadImages(false)

        assertFalse(fakeUserPreferencesRepository.preferencesFlow.value.loadImages)
    }

    @Test
    fun `given a mode when setArticleOpenMode is called then repository is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.setArticleOpenMode(ArticleOpenMode.EXTERNAL_BROWSER)

        assertEquals(ArticleOpenMode.EXTERNAL_BROWSER, fakeUserPreferencesRepository.preferencesFlow.value.articleOpenMode)
    }

    // toggleDailyReminder and setDailyReminderHour are intentionally not unit-tested here: they
    // call into DailyReminderScheduler, which requires an initialized WorkManager (only available
    // under Robolectric). That interaction is covered by SettingsScreenTest instead.

    @Test
    fun `given permission is denied when onNotificationPermissionResult is called then it emits ShowMessage event`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEvent.test {
            viewModel.onNotificationPermissionResult(false)

            val event = awaitItem()
            assertTrue(event is SettingsUiEvent.ShowMessage)
        }
    }

    @Test
    fun `given a name with surrounding whitespace when addTopic is called then it is trimmed`() = runTest {
        val viewModel = createViewModel()

        viewModel.addTopic("  Strategy  ")

        assertEquals(listOf(Fixtures.topic("Strategy")), fakeTopicsRepository.topicsFlow.value)
    }

    @Test
    fun `given a blank name when addTopic is called then it is ignored`() = runTest {
        val viewModel = createViewModel()

        viewModel.addTopic("   ")

        assertEquals(emptyList<Topic>(), fakeTopicsRepository.topicsFlow.value)
    }

    @Test
    fun `given an existing topic when removeTopic is called then repository removes it`() = runTest {
        val topic = Fixtures.topic("Action")
        fakeTopicsRepository.topicsFlow.value = listOf(topic)
        val viewModel = createViewModel()

        viewModel.removeTopic(topic)

        assertEquals(emptyList<Topic>(), fakeTopicsRepository.topicsFlow.value)
    }
}
