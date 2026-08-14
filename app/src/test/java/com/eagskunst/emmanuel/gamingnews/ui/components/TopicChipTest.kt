package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TopicChipTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `given a topic when the chip is rendered then the topic name is displayed`() {
        composeTestRule.setContent {
            TopicChip(topic = Topic("RPG"), onRemove = {})
        }

        composeTestRule.onNodeWithText("RPG").assertIsDisplayed()
    }

    @Test
    fun `when the remove icon is clicked then the removal callback is invoked`() {
        var removed = false
        composeTestRule.setContent {
            TopicChip(topic = Topic("RPG"), onRemove = { removed = true })
        }

        composeTestRule.onNodeWithContentDescription("Remove topic").performClick()
        assertTrue(removed)
    }
}
