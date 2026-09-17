package com.eagskunst.emmanuel.gamingnews.ui.settings.mutedwords

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidationError
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutedWordsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeMuteRulesRepository: FakeMuteRulesRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        fakeMuteRulesRepository = FakeMuteRulesRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    private fun createViewModel(): MutedWordsViewModel = MutedWordsViewModel(
        observeMuteRulesUseCase = ObserveMuteRulesUseCase(fakeMuteRulesRepository),
        saveMuteRuleUseCase = SaveMuteRuleUseCase(fakeMuteRulesRepository, TestDispatcherProvider()),
        deleteMuteRuleUseCase = DeleteMuteRuleUseCase(fakeMuteRulesRepository),
        updateApplyGlobalMuteRulesToReviewsUseCase =
            UpdateApplyGlobalMuteRulesToReviewsUseCase(fakeUserPreferencesRepository),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
    )

    @Test
    fun `given stored rules when initialized then they are listed`() = runTest {
        val rule = Fixtures.muteRule(text = "gta")
        fakeMuteRulesRepository.rulesFlow.value = listOf(rule)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(rule), state.rules)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `given a failing rules stream then loadFailed is set`() = runTest {
        fakeMuteRulesRepository.rulesError = IllegalStateException("db down")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.loadFailed)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `when openEditor is called then a default draft is shown`() = runTest {
        val viewModel = createViewModel()

        viewModel.openEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor
            assertNotNull(editor)
            assertNull(editor!!.ruleId)
            assertEquals(MuteMatchMode.CONTAINS, editor.matchMode)
            assertFalse(editor.caseSensitive)
            assertTrue(editor.scopeEverywhere)
        }
    }

    @Test
    fun `given an existing rule when openEditor is called then the draft restores every field`() = runTest {
        val rule = Fixtures.muteRule(
            id = "r1",
            text = "GTA",
            matchMode = MuteMatchMode.EXACT_PHRASE,
            caseSensitive = true,
            scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC, NewsCategory.SONY))
        )

        val viewModel = createViewModel()
        viewModel.openEditor(rule)

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertEquals("r1", editor.ruleId)
            assertEquals("GTA", editor.text)
            assertEquals(MuteMatchMode.EXACT_PHRASE, editor.matchMode)
            assertTrue(editor.caseSensitive)
            assertFalse(editor.scopeEverywhere)
            assertEquals(setOf(NewsCategory.PC, NewsCategory.SONY), editor.selectedTabs)
        }
    }

    @Test
    fun `given a blank draft when saveEditor is called then a blank-text error is shown and nothing is saved`() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("   ")

        viewModel.saveEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertTrue(MuteRuleValidationError.BLANK_TEXT in editor.errors)
        }
        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
    }

    @Test
    fun `given a multi-word whole-word draft when saveEditor is called then a whole-word error is shown`() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta vi")
        viewModel.onEditorMatchModeChange(MuteMatchMode.WHOLE_WORD)

        viewModel.saveEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertTrue(MuteRuleValidationError.WHOLE_WORD_MULTI_WORD in editor.errors)
        }
        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
    }

    @Test
    fun `given an empty tab selection when saveEditor is called then a no-tabs error is shown`() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta")
        viewModel.onEditorScopeEverywhereChange(false)

        viewModel.saveEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertTrue(MuteRuleValidationError.EMPTY_TAB_SELECTION in editor.errors)
        }
    }

    @Test
    fun `given a duplicate draft when saveEditor is called then a duplicate error is shown`() = runTest {
        fakeMuteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(id = "existing", text = "GTA")
        )
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta")

        viewModel.saveEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertTrue(MuteRuleValidationError.DUPLICATE_RULE in editor.errors)
        }
        assertEquals(1, fakeMuteRulesRepository.rulesFlow.value.size)
    }

    @Test
    fun `given a valid draft when saveEditor is called then the rule is saved and the sheet closes`() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta")
        viewModel.onEditorScopeEverywhereChange(false)
        viewModel.onEditorTabToggle(NewsCategory.PC)

        viewModel.saveEditor()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertNull(state.editor)
            assertEquals(1, state.rules.size)
            assertEquals(MuteScope.SelectedTabs(setOf(NewsCategory.PC)), state.rules[0].scope)
        }
    }

    @Test
    fun `given an open editor when cancelEditor is called then the sheet closes without saving`() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta")

        viewModel.cancelEditor()

        viewModel.uiState.test {
            assertNull(expectMostRecentItem().editor)
        }
        assertTrue(fakeMuteRulesRepository.rulesFlow.value.isEmpty())
    }

    @Test
    fun `given a failing write when saveEditor is called then the sheet stays open with a failure flag`() = runTest {
        fakeMuteRulesRepository.writeError = IllegalStateException("disk full")
        fakeMuteRulesRepository.enforceDuplicates = false
        val viewModel = createViewModel()
        viewModel.openEditor()
        viewModel.onEditorTextChange("gta")

        viewModel.saveEditor()

        viewModel.uiState.test {
            val editor = expectMostRecentItem().editor!!
            assertTrue(editor.saveFailed)
            assertFalse(editor.isSaving)
        }
    }

    @Test
    fun `given a pending delete when confirmDeleteRule is called then the rule is removed`() = runTest {
        val rule = Fixtures.muteRule(id = "r1", text = "gta")
        fakeMuteRulesRepository.rulesFlow.value = listOf(rule)
        val viewModel = createViewModel()

        viewModel.requestDeleteRule(rule)
        viewModel.uiState.test {
            assertEquals(rule, expectMostRecentItem().pendingDelete)
        }

        viewModel.confirmDeleteRule()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertNull(state.pendingDelete)
            assertTrue(state.rules.isEmpty())
        }
    }

    @Test
    fun `given a pending delete when dismissDeleteRule is called then the rule is kept`() = runTest {
        val rule = Fixtures.muteRule(id = "r1", text = "gta")
        fakeMuteRulesRepository.rulesFlow.value = listOf(rule)
        val viewModel = createViewModel()

        viewModel.requestDeleteRule(rule)
        viewModel.dismissDeleteRule()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertNull(state.pendingDelete)
            assertEquals(listOf(rule), state.rules)
        }
    }

    @Test
    fun `when toggleApplyToReviews is called then the preference is persisted`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleApplyToReviews(true)

        assertTrue(
            fakeUserPreferencesRepository.preferencesFlow.value.applyGlobalMuteRulesToReviews
        )
    }
}
