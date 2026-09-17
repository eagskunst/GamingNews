package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleMuteMatcherTest {

    private fun matches(title: String, rule: MuteRule): Boolean =
        TitleMuteMatcher.matches(rule, TitleMuteMatcher.normalizeWhitespace(title))

    // region Contains

    @Test
    fun `given a contains rule when the title has the text anywhere then it matches`() {
        val rule = Fixtures.muteRule(text = "gta", matchMode = MuteMatchMode.CONTAINS)

        assertTrue(matches("The GTA VI trailer is out", rule))
        assertTrue(matches("GTA at the start", rule))
        assertTrue(matches("ends with GTA", rule))
    }

    @Test
    fun `given a contains rule when the title lacks the text then it does not match`() {
        val rule = Fixtures.muteRule(text = "gta", matchMode = MuteMatchMode.CONTAINS)

        assertFalse(matches("Hollow Knight review", rule))
    }

    @Test
    fun `given a contains rule when the text is inside another word then it still matches`() {
        val rule = Fixtures.muteRule(text = "war", matchMode = MuteMatchMode.CONTAINS)

        assertTrue(matches("Star Wars out now", rule))
        assertTrue(matches("Warmachine announced", rule))
    }

    // endregion

    // region Case sensitivity

    @Test
    fun `given a case-insensitive rule when casing differs then it matches`() {
        val rule = Fixtures.muteRule(text = "gta", caseSensitive = false)

        assertTrue(matches("GTA VI is out", rule))
        assertTrue(matches("gTa Vi is out", rule))
    }

    @Test
    fun `given a case-sensitive rule when casing differs then it does not match`() {
        val rule = Fixtures.muteRule(text = "GTA", caseSensitive = true)

        assertFalse(matches("gta VI is out", rule))
        assertTrue(matches("GTA VI is out", rule))
    }

    @Test
    fun `given accented text when case-insensitive then accents and casing are preserved and folded`() {
        val rule = Fixtures.muteRule(text = "café", caseSensitive = false)

        assertTrue(matches("Café Story review", rule))
        assertTrue(matches("CAFÉ Story review", rule))
        assertFalse(matches("Cafe Story review", rule))
    }

    // endregion

    // region Whole word

    @Test
    fun `given a whole word rule when the word appears whole then it matches`() {
        val rule = Fixtures.muteRule(text = "war", matchMode = MuteMatchMode.WHOLE_WORD)

        assertTrue(matches("Star Wars? No, war games", rule))
        assertTrue(matches("War is coming", rule))
        assertTrue(matches("The war, again", rule))
        assertTrue(matches("a (war) story", rule))
    }

    @Test
    fun `given a whole word rule when the word is part of another then it does not match`() {
        val rule = Fixtures.muteRule(text = "war", matchMode = MuteMatchMode.WHOLE_WORD)

        assertFalse(matches("Star Wars trailer", rule))
        assertFalse(matches("Warmachine announced", rule))
        assertFalse(matches("toward the end", rule))
        assertFalse(matches("warzone update", rule))
    }

    @Test
    fun `given a whole word rule when followed by digits then it does not match`() {
        val rule = Fixtures.muteRule(text = "zelda", matchMode = MuteMatchMode.WHOLE_WORD)

        assertFalse(matches("zelda2 announced", rule))
        assertTrue(matches("Zelda 2 announced", rule))
    }

    @Test
    fun `given a whole word rule when the title has combining marks then boundaries are unicode aware`() {
        // "café" written with a combining acute accent (e + U+0301) must keep matching as one word.
        val combining = "Café Story"
        val rule = Fixtures.muteRule(text = "café", matchMode = MuteMatchMode.WHOLE_WORD)

        assertFalse(matches("Cafeteria Story", rule))
        assertTrue(matches(combining, rule))
    }

    // endregion

    // region Exact phrase

    @Test
    fun `given an exact phrase rule when the phrase appears whole then it matches`() {
        val rule = Fixtures.muteRule(text = "breath of the wild", matchMode = MuteMatchMode.EXACT_PHRASE)

        assertTrue(matches("Zelda: Breath of the Wild review", rule))
        assertTrue(matches("Breath of the Wild", rule))
    }

    @Test
    fun `given an exact phrase rule when the phrase bleeds into a longer word then it does not match`() {
        val rule = Fixtures.muteRule(text = "the wild", matchMode = MuteMatchMode.EXACT_PHRASE)

        assertFalse(matches("into the wilderness", rule))
        assertFalse(matches("the wildcard entry", rule))
        assertTrue(matches("into the wild, again", rule))
    }

    @Test
    fun `given an exact phrase rule when whitespace differs then collapsed whitespace still matches`() {
        val rule = Fixtures.muteRule(text = "gta   vi", matchMode = MuteMatchMode.EXACT_PHRASE)

        assertTrue(matches("GTA VI announced", rule))
        assertTrue(matches("GTA  VI announced", rule))
        assertFalse(matches("GTA VII announced", rule))
    }

    // endregion

    // region Scope resolution

    @Test
    fun `given an everywhere rule then it matches in every context`() {
        val rule = Fixtures.muteRule(text = "gta", scope = MuteScope.Everywhere)

        NewsCategory.entries.forEach { category ->
            assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(category)).matches("GTA VI"))
        }
        assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.Reviews).matches("GTA VI"))
        assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.Global).matches("GTA VI"))
    }

    @Test
    fun `given a selected-tabs rule then it only matches the selected tabs`() {
        val rule = Fixtures.muteRule(
            text = "gta",
            scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC, NewsCategory.SONY))
        )

        assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(NewsCategory.PC)).matches("GTA VI"))
        assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(NewsCategory.SONY)).matches("GTA VI"))
        assertFalse(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(NewsCategory.NINTENDO)).matches("GTA VI"))
        assertFalse(PreparedTitleMuteMatcher(listOf(rule), MuteContext.Reviews).matches("GTA VI"))
        assertFalse(PreparedTitleMuteMatcher(listOf(rule), MuteContext.Global).matches("GTA VI"))
    }

    @Test
    fun `given a rule scoped to the all-news tab then it does not leak into other tabs`() {
        val rule = Fixtures.muteRule(
            text = "gta",
            scope = MuteScope.SelectedTabs(setOf(NewsCategory.ALL))
        )

        assertTrue(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(NewsCategory.ALL)).matches("GTA VI"))
        assertFalse(PreparedTitleMuteMatcher(listOf(rule), MuteContext.NewsTab(NewsCategory.PC)).matches("GTA VI"))
    }

    @Test
    fun `given several rules when one matches then the title is muted (or semantics)`() {
        val rules = listOf(
            Fixtures.muteRule(id = "1", text = "zzz no match"),
            Fixtures.muteRule(id = "2", text = "nintendo"),
            Fixtures.muteRule(id = "3", text = "also no match")
        )

        val matcher = PreparedTitleMuteMatcher(rules, MuteContext.NewsTab(NewsCategory.ALL))

        assertTrue(matcher.matches("New Nintendo hardware"))
        assertFalse(matcher.matches("New Sony hardware"))
    }

    // endregion

    @Test
    fun `when normalizing whitespace then it trims and collapses runs`() {
        assertEquals("gta vi", TitleMuteMatcher.normalizeWhitespace("  gta \t\n vi  "))
        assertEquals("", TitleMuteMatcher.normalizeWhitespace("   "))
    }

    @Test
    fun `given a blank rule text then nothing matches`() {
        val rule = Fixtures.muteRule(text = "   ")

        assertFalse(matches("GTA VI", rule))
    }
}
