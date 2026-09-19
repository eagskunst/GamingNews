package com.eagskunst.emmanuel.gamingnews.ui.settings.mutedwords

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.DeleteMuteRuleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveMuteRulesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SaveMuteRuleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateApplyGlobalMuteRulesToReviewsUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeMuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MutedWordsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeMuteRulesRepository = FakeMuteRulesRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    private fun createViewModel() = MutedWordsViewModel(
        observeMuteRulesUseCase = ObserveMuteRulesUseCase(fakeMuteRulesRepository),
        saveMuteRuleUseCase = SaveMuteRuleUseCase(fakeMuteRulesRepository, TestDispatcherProvider()),
        deleteMuteRuleUseCase = DeleteMuteRuleUseCase(fakeMuteRulesRepository),
        updateApplyGlobalMuteRulesToReviewsUseCase =
            UpdateApplyGlobalMuteRulesToReviewsUseCase(fakeUserPreferencesRepository),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
    )

    private fun setContent(viewModel: MutedWordsViewModel = createViewModel()): MutedWordsViewModel {
        composeTestRule.setContent {
            MutedWordsScreen(viewModel = viewModel, onBackClick = {})
        }
        return viewModel
    }

    private fun openAddSheet(viewModel: MutedWordsViewModel) {
        viewModel.openEditor()
        composeTestRule.waitForIdle()
    }

    private fun tapSheetButton(label: String) {
        composeTestRule
            .onNodeWithText(label)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun `given no rules then the empty state and add action are shown`() {
        setContent()

        composeTestRule.onNodeWithText("No muted words yet").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Add muted word", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Apply global mute rules to Reviews").assertIsDisplayed()
    }

    @Test
    fun `given stored rules then the list shows text, mode, scope and the case badge`() {
        fakeMuteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(
                id = "r1",
                text = "GTA",
                matchMode = MuteMatchMode.WHOLE_WORD,
                caseSensitive = true,
                scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC))
            ),
            Fixtures.muteRule(id = "r2", text = "zelda")
        )
        setContent()

        composeTestRule.onNodeWithText("GTA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Whole word · PC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Case sensitive").assertIsDisplayed()
        composeTestRule.onNodeWithText("zelda").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contains · Everywhere").assertIsDisplayed()
    }

    @Test
    fun `given the add flow when save is tapped then the rule is persisted`() {
        val viewModel = setContent()

        openAddSheet(viewModel)
        composeTestRule.onNode(hasSetTextAction()).performTextInput("gta")
        tapSheetButton("Save")

        assertEquals(1, fakeMuteRulesRepository.rulesFlow.value.size)
        assertEquals("gta", fakeMuteRulesRepository.rulesFlow.value[0].text)
    }

    @Test
    fun `given the add flow when cancel is tapped then nothing is saved`() {
        val viewModel = setContent()

        openAddSheet(viewModel)
        tapSheetButton("CANCEL")

        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
        composeTestRule.onNodeWithText("No muted words yet").assertIsDisplayed()
    }

    @Test
    fun `given a blank entry when save is tapped then the validation error is shown`() {
        val viewModel = setContent()

        openAddSheet(viewModel)
        tapSheetButton("Save")

        composeTestRule.onNodeWithText("Enter a word or phrase").assertIsDisplayed()
        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
    }

    @Test
    fun `given an existing rule when its row is tapped then the editor restores every field`() {
        fakeMuteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(
                id = "r1",
                text = "GTA",
                matchMode = MuteMatchMode.WHOLE_WORD,
                caseSensitive = true,
                scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC))
            )
        )
        setContent()

        composeTestRule.onNodeWithText("GTA").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Edit muted word").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertExists()
    }

    @Test
    fun `given an existing rule when delete is confirmed then the rule is removed`() {
        fakeMuteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(id = "r1", text = "gta")
        )
        setContent()

        composeTestRule.onNodeWithText("gta").performClick()
        composeTestRule.waitForIdle()
        tapSheetButton("Delete")

        composeTestRule.onNodeWithText("Delete muted word?").assertIsDisplayed()
        // Two "Delete" nodes exist: the sheet's and the dialog's; the dialog draws last.
        composeTestRule.onAllNodesWithText("Delete")[1].performClick()
        composeTestRule.waitForIdle()

        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
    }

    @Test
    fun `when the reviews switch is toggled then the preference is persisted`() {
        setContent()

        val switch = composeTestRule.onAllNodes(isToggleable())[0]
        switch.assertIsOff()
        switch.performClick()
        composeTestRule.waitForIdle()

        assertTrue(
            fakeUserPreferencesRepository.preferencesFlow.value.applyGlobalMuteRulesToReviews
        )
        composeTestRule.onAllNodes(isToggleable())[0].assertIsOn()
    }
}
