package com.eagskunst.emmanuel.gamingnews.ui.settings.mutedwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleDraft
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidationError
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidator
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.DeleteMuteRuleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveMuteRulesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SaveMuteRuleResult
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SaveMuteRuleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateApplyGlobalMuteRulesToReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MutedWordsUiState(
    val rules: List<MuteRule> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val applyToReviews: Boolean = false,
    val editor: MuteRuleEditorState? = null,
    val pendingDelete: MuteRule? = null
)

/**
 * Editor draft kept in the ViewModel so it survives configuration changes while the sheet is open.
 */
data class MuteRuleEditorState(
    val ruleId: String? = null,
    val text: String = "",
    val matchMode: MuteMatchMode = MuteMatchMode.CONTAINS,
    val caseSensitive: Boolean = false,
    val scopeEverywhere: Boolean = true,
    val selectedTabs: Set<NewsCategory> = emptySet(),
    val errors: Set<MuteRuleValidationError> = emptySet(),
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false
) {
    val isEditingExisting: Boolean get() = ruleId != null

    fun toDraft(): MuteRuleDraft = MuteRuleDraft(
        id = ruleId,
        text = text,
        matchMode = matchMode,
        caseSensitive = caseSensitive,
        scopeEverywhere = scopeEverywhere,
        selectedTabs = selectedTabs
    )
}

private fun MuteRule.toEditorState() = MuteRuleEditorState(
    ruleId = id,
    text = text,
    matchMode = matchMode,
    caseSensitive = caseSensitive,
    scopeEverywhere = scope is MuteScope.Everywhere,
    selectedTabs = (scope as? MuteScope.SelectedTabs)?.tabs.orEmpty()
)

@HiltViewModel
class MutedWordsViewModel @Inject constructor(
    private val observeMuteRulesUseCase: ObserveMuteRulesUseCase,
    private val saveMuteRuleUseCase: SaveMuteRuleUseCase,
    private val deleteMuteRuleUseCase: DeleteMuteRuleUseCase,
    private val updateApplyGlobalMuteRulesToReviewsUseCase: UpdateApplyGlobalMuteRulesToReviewsUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MutedWordsUiState())
    val uiState: StateFlow<MutedWordsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeMuteRulesUseCase()
                .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
                .collect { rules ->
                    _uiState.update { it.copy(rules = rules, isLoading = false, loadFailed = false) }
                }
        }
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { preferences ->
                _uiState.update { it.copy(applyToReviews = preferences.applyGlobalMuteRulesToReviews) }
            }
        }
    }

    fun openEditor(rule: MuteRule? = null) {
        _uiState.update { it.copy(editor = rule?.toEditorState() ?: MuteRuleEditorState()) }
    }

    fun cancelEditor() {
        _uiState.update { it.copy(editor = null) }
    }

    private fun updateEditor(transform: (MuteRuleEditorState) -> MuteRuleEditorState) {
        _uiState.update { state -> state.copy(editor = state.editor?.let(transform)) }
    }

    fun onEditorTextChange(text: String) {
        updateEditor { it.copy(text = text, errors = emptySet(), saveFailed = false) }
    }

    fun onEditorMatchModeChange(mode: MuteMatchMode) {
        updateEditor { it.copy(matchMode = mode, errors = emptySet(), saveFailed = false) }
    }

    fun onEditorCaseSensitiveChange(caseSensitive: Boolean) {
        updateEditor { it.copy(caseSensitive = caseSensitive, errors = emptySet(), saveFailed = false) }
    }

    fun onEditorScopeEverywhereChange(everywhere: Boolean) {
        updateEditor { it.copy(scopeEverywhere = everywhere, errors = emptySet(), saveFailed = false) }
    }

    fun onEditorTabToggle(category: NewsCategory) {
        updateEditor {
            val tabs = if (category in it.selectedTabs) {
                it.selectedTabs - category
            } else {
                it.selectedTabs + category
            }
            it.copy(selectedTabs = tabs, errors = emptySet(), saveFailed = false)
        }
    }

    fun saveEditor() {
        val editor = _uiState.value.editor ?: return
        if (editor.isSaving) return
        val draft = editor.toDraft()

        val errors = MuteRuleValidator.validate(draft, _uiState.value.rules)
        if (errors.isNotEmpty()) {
            updateEditor { it.copy(errors = errors) }
            return
        }

        updateEditor { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            when (val result = saveMuteRuleUseCase(draft)) {
                SaveMuteRuleResult.Success -> _uiState.update { it.copy(editor = null) }
                is SaveMuteRuleResult.Invalid -> updateEditor {
                    it.copy(isSaving = false, errors = result.errors)
                }
                is SaveMuteRuleResult.Failure -> updateEditor {
                    it.copy(isSaving = false, saveFailed = true)
                }
            }
        }
    }

    fun requestDeleteRule(rule: MuteRule) {
        _uiState.update { it.copy(pendingDelete = rule) }
    }

    fun dismissDeleteRule() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDeleteRule() {
        val rule = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null, editor = null) }
        viewModelScope.launch {
            try {
                deleteMuteRuleUseCase(rule.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loadFailed = true) }
            }
        }
    }

    fun toggleApplyToReviews(enabled: Boolean) {
        _uiState.update { it.copy(applyToReviews = enabled) }
        viewModelScope.launch {
            try {
                updateApplyGlobalMuteRulesToReviewsUseCase(enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(applyToReviews = !enabled) }
            }
        }
    }
}
