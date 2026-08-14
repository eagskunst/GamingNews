package com.eagskunst.emmanuel.gamingnews.ui.settings

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDynamicColorUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateThemeModeUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeTopicsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    private val fakeTopicsRepository = FakeTopicsRepository()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
        } catch (_: IllegalStateException) {
            // WorkManager was already initialized by a previous test.
        }
    }

    private fun createViewModel(): SettingsViewModel {
        val context = ApplicationProvider.getApplicationContext<Application>()
        return SettingsViewModel(
            context = context,
            getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
            getTopicsUseCase = GetTopicsUseCase(fakeTopicsRepository),
            updateThemeModeUseCase = UpdateThemeModeUseCase(fakeUserPreferencesRepository),
            updateDynamicColorUseCase = UpdateDynamicColorUseCase(fakeUserPreferencesRepository),
            updateDarkThemeUseCase = UpdateDarkThemeUseCase(fakeUserPreferencesRepository),
            updateLoadImagesUseCase = UpdateLoadImagesUseCase(fakeUserPreferencesRepository),
            updateDailyReminderUseCase = UpdateDailyReminderUseCase(fakeUserPreferencesRepository),
            updateArticleOpenModeUseCase = UpdateArticleOpenModeUseCase(fakeUserPreferencesRepository),
            addTopicUseCase = AddTopicUseCase(fakeTopicsRepository),
            removeTopicUseCase = RemoveTopicUseCase(fakeTopicsRepository)
        )
    }

    private fun setContent(viewModel: SettingsViewModel = createViewModel()) {
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = {},
                onContactEmailClick = {},
                onContactWebsiteClick = {},
                onPrivacyPolicyClick = {}
            )
        }
    }

    @Test
    fun `given theme mode is system when changed to dark in dialog then preference is updated to DARK`() {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(themeMode = ThemeMode.SYSTEM)
        setContent()

        composeTestRule.onNodeWithText("Theme").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.waitForIdle()

        assertEquals(ThemeMode.DARK, fakeUserPreferencesRepository.preferencesFlow.value.themeMode)
    }

    @Test
    fun `given the dynamic color switch is on when it is toggled then the preference is disabled and switch is off`() {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(dynamicColor = true)
        setContent()

        val switch = composeTestRule.onAllNodes(isToggleable())[0]
        switch.assertIsOn()
        switch.performClick()
        composeTestRule.waitForIdle()

        assertFalse(fakeUserPreferencesRepository.preferencesFlow.value.dynamicColor)
        composeTestRule.onAllNodes(isToggleable())[0].assertIsOff()
    }

    @Test
    fun `given the load images switch is on when it is toggled then the preference is disabled and the switch is off`() {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(loadImages = true)
        setContent()

        val switch = composeTestRule.onAllNodes(isToggleable())[1]
        switch.assertIsOn()
        switch.performClick()
        composeTestRule.waitForIdle()

        assertFalse(fakeUserPreferencesRepository.preferencesFlow.value.loadImages)
        composeTestRule.onAllNodes(isToggleable())[1].assertIsOff()
    }

    @Test
    fun `given the daily reminder switch is off when it is toggled then the preference is enabled and the switch is on`() {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(dailyReminder = false)
        setContent()

        val switch = composeTestRule.onAllNodes(isToggleable())[2]
        switch.assertIsOff()
        switch.performClick()
        composeTestRule.waitForIdle()

        assertTrue(fakeUserPreferencesRepository.preferencesFlow.value.dailyReminder)
        composeTestRule.onAllNodes(isToggleable())[2].assertIsOn()
    }
}
