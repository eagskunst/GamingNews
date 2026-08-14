package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.eagskunst.emmanuel.gamingnews.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainTopAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `given a title when the top app bar is rendered then the title is displayed`() {
        composeTestRule.setContent {
            MainTopAppBar(
                title = "Top Bar Title",
                searchQuery = "",
                onSearchQueryChange = {},
                onSettingsClick = {}
            )
        }

        composeTestRule.onNodeWithText("Top Bar Title").assertIsDisplayed()
    }

    @Test
    fun `when the search icon is toggled then the query can be entered and cleared`() {
        var query = ""
        composeTestRule.setContent {
            MainTopAppBar(
                title = "Top Bar Title",
                searchQuery = query,
                onSearchQueryChange = { query = it },
                onSettingsClick = {}
            )
        }

        val searchHint = composeTestRule.activity.getString(R.string.search_hint)
        composeTestRule.onNodeWithContentDescription(searchHint).performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("query text")
        assertEquals("query text", query)

        composeTestRule.onNodeWithContentDescription(searchHint).performClick()
        assertEquals("", query)
        composeTestRule.onNodeWithText("Top Bar Title").assertIsDisplayed()
    }

    @Test
    fun `when the settings icon is clicked then the settings callback is invoked`() {
        var settingsClicked = false
        composeTestRule.setContent {
            MainTopAppBar(
                title = "Top Bar Title",
                searchQuery = "",
                onSearchQueryChange = {},
                onSettingsClick = { settingsClicked = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.title_activity_settings))
            .performClick()
        assertTrue(settingsClicked)
    }
}
