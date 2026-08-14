package com.eagskunst.emmanuel.gamingnews.ui.topics

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeTopicsRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TopicsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeTopicsRepository = FakeTopicsRepository()

    private fun createViewModel(): TopicsViewModel = TopicsViewModel(
        getTopicsUseCase = GetTopicsUseCase(fakeTopicsRepository),
        addTopicUseCase = AddTopicUseCase(fakeTopicsRepository),
        removeTopicUseCase = RemoveTopicUseCase(fakeTopicsRepository)
    )

    private fun setContent(viewModel: TopicsViewModel = createViewModel()) {
        composeTestRule.setContent {
            TopicsScreen(viewModel = viewModel)
        }
    }

    @Test
    fun `given a list of topics when the screen is shown then they are rendered as chips sorted by name`() {
        fakeTopicsRepository.topicsFlow.value = listOf(
            Topic("Zelda"),
            Topic("Mario"),
            Topic("Donkey Kong")
        )
        setContent()

        composeTestRule.onNodeWithText("Donkey Kong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zelda").assertIsDisplayed()
    }

    @Test
    fun `given a topic name is entered in the add topic dialog when confirmed then the topic is added and displayed`() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Add topic").performClick()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.add_topic))
            .assertIsDisplayed()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("Metroid")
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.add), ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Metroid").assertIsDisplayed()
        assertTrue(fakeTopicsRepository.topicsFlow.value.contains(Topic("Metroid")))
    }

    @Test
    fun `given a topic chip when the remove action is clicked then the topic is removed`() {
        fakeTopicsRepository.topicsFlow.value = listOf(
            Topic("Apple"),
            Topic("Banana")
        )
        setContent()

        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banana").assertIsDisplayed()

        composeTestRule.onAllNodesWithContentDescription("Remove topic")[0].performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Apple").assertDoesNotExist()
        composeTestRule.onNodeWithText("Banana").assertIsDisplayed()
        assertFalse(fakeTopicsRepository.topicsFlow.value.contains(Topic("Apple")))
    }
}
