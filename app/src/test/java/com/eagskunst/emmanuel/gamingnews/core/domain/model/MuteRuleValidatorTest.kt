package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuteRuleValidatorTest {

    private fun validate(draft: MuteRuleDraft, existing: List<MuteRule> = emptyList()) =
        MuteRuleValidator.validate(draft, existing)

    @Test
    fun `given a blank text when validating then blank error is returned`() {
        val errors = validate(MuteRuleDraft(text = "   "))

        assertTrue(MuteRuleValidationError.BLANK_TEXT in errors)
    }

    @Test
    fun `given a multi-word whole-word draft when validating then whole-word error is returned`() {
        val errors = validate(MuteRuleDraft(text = "gta vi", matchMode = MuteMatchMode.WHOLE_WORD))

        assertTrue(MuteRuleValidationError.WHOLE_WORD_MULTI_WORD in errors)
    }

    @Test
    fun `given a single-word whole-word draft when validating then no error is returned`() {
        val errors = validate(MuteRuleDraft(text = "gta", matchMode = MuteMatchMode.WHOLE_WORD))

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `given a multi-word exact-phrase draft when validating then no error is returned`() {
        val errors = validate(MuteRuleDraft(text = "gta vi", matchMode = MuteMatchMode.EXACT_PHRASE))

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `given a selected-tabs scope with no tabs when validating then empty-selection error is returned`() {
        val errors = validate(
            MuteRuleDraft(text = "gta", scopeEverywhere = false, selectedTabs = emptySet())
        )

        assertTrue(MuteRuleValidationError.EMPTY_TAB_SELECTION in errors)
    }

    @Test
    fun `given a duplicate rule when validating then duplicate error is returned`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "gta"))
        val errors = validate(MuteRuleDraft(text = "gta"), existing)

        assertEquals(setOf(MuteRuleValidationError.DUPLICATE_RULE), errors)
    }

    @Test
    fun `given case-insensitive rules with different casing when validating then they are duplicates`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "GTA", caseSensitive = false))
        val errors = validate(MuteRuleDraft(text = "gta", caseSensitive = false), existing)

        assertTrue(MuteRuleValidationError.DUPLICATE_RULE in errors)
    }

    @Test
    fun `given case-sensitive rules with different casing when validating then they are not duplicates`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "GTA", caseSensitive = true))
        val errors = validate(MuteRuleDraft(text = "gta", caseSensitive = true), existing)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `given rules differing only in case sensitivity when validating then they may coexist`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "gta", caseSensitive = false))
        val errors = validate(MuteRuleDraft(text = "gta", caseSensitive = true), existing)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `given rules differing in match mode or scope when validating then they are not duplicates`() {
        val existing = listOf(
            Fixtures.muteRule(id = "1", text = "gta", matchMode = MuteMatchMode.CONTAINS),
            Fixtures.muteRule(id = "2", text = "gta", scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC)))
        )

        assertTrue(validate(MuteRuleDraft(text = "gta", matchMode = MuteMatchMode.WHOLE_WORD), existing).isEmpty())
        assertTrue(
            validate(
                MuteRuleDraft(text = "gta", scopeEverywhere = false, selectedTabs = setOf(NewsCategory.SONY)),
                existing
            ).isEmpty()
        )
    }

    @Test
    fun `given the same tab set in a different order when validating then it is a duplicate`() {
        val existing = listOf(
            Fixtures.muteRule(
                id = "1",
                text = "gta",
                scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC, NewsCategory.SONY))
            )
        )
        val errors = validate(
            MuteRuleDraft(
                text = "gta",
                scopeEverywhere = false,
                selectedTabs = linkedSetOf(NewsCategory.SONY, NewsCategory.PC)
            ),
            existing
        )

        assertTrue(MuteRuleValidationError.DUPLICATE_RULE in errors)
    }

    @Test
    fun `given equivalent text with extra whitespace when validating then it is a duplicate`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "gta vi", matchMode = MuteMatchMode.EXACT_PHRASE))
        val errors = validate(
            MuteRuleDraft(text = "  gta    vi ", matchMode = MuteMatchMode.EXACT_PHRASE),
            existing
        )

        assertTrue(MuteRuleValidationError.DUPLICATE_RULE in errors)
    }

    @Test
    fun `given an edit of an existing rule when validating then it does not duplicate itself`() {
        val existing = listOf(Fixtures.muteRule(id = "1", text = "gta"))
        val errors = validate(MuteRuleDraft(id = "1", text = "gta"), existing)

        assertTrue(errors.isEmpty())
    }
}
