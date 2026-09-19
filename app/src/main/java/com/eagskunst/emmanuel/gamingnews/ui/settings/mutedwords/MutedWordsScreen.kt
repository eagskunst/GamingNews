package com.eagskunst.emmanuel.gamingnews.ui.settings.mutedwords

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidationError
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.ui.components.displayName

private val categories = NewsCategory.entries.toTypedArray()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedWordsScreen(
    viewModel: MutedWordsViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.muted_words_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openEditor() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.muted_words_add)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = stringResource(R.string.muted_words_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ReviewsOptInRow(
                checked = uiState.applyToReviews,
                onCheckedChange = viewModel::toggleApplyToReviews
            )

            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.loadFailed -> {
                    Text(
                        text = stringResource(R.string.mute_rules_load_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }

                uiState.rules.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.muted_words_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.rules, key = { it.id }) { rule ->
                            MuteRuleRow(
                                rule = rule,
                                onClick = { viewModel.openEditor(rule) }
                            )
                        }
                    }
                }
            }
        }
    }
    uiState.editor?.let { editor ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        MuteRuleEditorSheet(
            editor = editor,
            onTextChange = viewModel::onEditorTextChange,
            onMatchModeChange = viewModel::onEditorMatchModeChange,
            onCaseSensitiveChange = viewModel::onEditorCaseSensitiveChange,
            onScopeEverywhereChange = viewModel::onEditorScopeEverywhereChange,
            onTabToggle = viewModel::onEditorTabToggle,
            onSave = viewModel::saveEditor,
            onDelete = {
                uiState.rules.firstOrNull { it.id == editor.ruleId }
                    ?.let(viewModel::requestDeleteRule)
            },
            onDismiss = viewModel::cancelEditor,
            sheetState = sheetState
        )
    }

    uiState.pendingDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteRule,
            title = { Text(stringResource(R.string.mute_rule_delete_confirm_title)) },
            text = { Text(stringResource(R.string.mute_rule_delete_confirm_message, rule.text)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteRule) {
                    Text(stringResource(R.string.mute_rule_delete).uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteRule) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ReviewsOptInRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp)) {
            Text(
                text = stringResource(R.string.mute_rules_reviews_switch),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.mute_rules_reviews_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MuteRuleRow(
    rule: MuteRule,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = ruleScopeSummary(rule),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (rule.caseSensitive) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(R.string.mute_rule_case_badge),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ruleScopeSummary(rule: MuteRule): String {
    val modeLabel = stringResource(
        when (rule.matchMode) {
            MuteMatchMode.CONTAINS -> R.string.mute_match_contains
            MuteMatchMode.WHOLE_WORD -> R.string.mute_match_whole_word
            MuteMatchMode.EXACT_PHRASE -> R.string.mute_match_exact_phrase
        }
    )
    val scopeLabel = when (val scope = rule.scope) {
        MuteScope.Everywhere -> stringResource(R.string.mute_scope_everywhere)
        is MuteScope.SelectedTabs -> {
            val labels = mutableListOf<String>()
            for (tab in scope.tabs.sortedBy { it.ordinal }) {
                labels += tab.displayName()
            }
            labels.joinToString(", ")
        }
    }
    return "$modeLabel · $scopeLabel"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MuteRuleEditorSheet(
    editor: MuteRuleEditorState,
    onTextChange: (String) -> Unit,
    onMatchModeChange: (MuteMatchMode) -> Unit,
    onCaseSensitiveChange: (Boolean) -> Unit,
    onScopeEverywhereChange: (Boolean) -> Unit,
    onTabToggle: (NewsCategory) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Insets handled manually so the scroll viewport — not just the bottom of the
        // scrollable content — stays above the system navigation bar on pre-API-35
        // edge-to-edge devices.
        contentWindowInsets = { WindowInsets(0) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(
                    if (editor.isEditingExisting) R.string.mute_rule_edit_title
                    else R.string.mute_rule_add_title
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editor.text,
                onValueChange = onTextChange,
                label = { Text(stringResource(R.string.mute_rule_text_label)) },
                isError = MuteRuleValidationError.BLANK_TEXT in editor.errors ||
                        MuteRuleValidationError.WHOLE_WORD_MULTI_WORD in editor.errors ||
                        MuteRuleValidationError.DUPLICATE_RULE in editor.errors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val errorText = editorErrorText(editor.errors)
            if (errorText != null || editor.saveFailed) {
                Text(
                    text = errorText ?: stringResource(R.string.mute_rule_save_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.mute_rule_match_mode),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MuteMatchMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = editor.matchMode == mode,
                        onClick = { onMatchModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = MuteMatchMode.entries.size
                        )
                    ) {
                        Text(
                            text = stringResource(
                                when (mode) {
                                    MuteMatchMode.CONTAINS -> R.string.mute_match_contains
                                    MuteMatchMode.WHOLE_WORD -> R.string.mute_match_whole_word
                                    MuteMatchMode.EXACT_PHRASE -> R.string.mute_match_exact_phrase
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)) {
                    Text(
                        text = stringResource(R.string.mute_rule_case_sensitive),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.mute_rule_case_sensitive_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = editor.caseSensitive,
                    onCheckedChange = onCaseSensitiveChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.mute_rule_scope),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = editor.scopeEverywhere,
                    onClick = { onScopeEverywhereChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.mute_scope_everywhere))
                }
                SegmentedButton(
                    selected = !editor.scopeEverywhere,
                    onClick = { onScopeEverywhereChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.mute_scope_selected_tabs))
                }
            }

            if (!editor.scopeEverywhere) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = category in editor.selectedTabs,
                            onClick = { onTabToggle(category) },
                            label = { Text(category.displayName()) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editor.isEditingExisting) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.mute_rule_delete).uppercase(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = onSave,
                    enabled = !editor.isSaving
                ) {
                    Text(stringResource(R.string.mute_rule_save).uppercase())
                }
            }
        }
    }
}

@Composable
private fun editorErrorText(errors: Set<MuteRuleValidationError>): String? {
    val res = when {
        MuteRuleValidationError.BLANK_TEXT in errors -> R.string.mute_rule_error_blank
        MuteRuleValidationError.WHOLE_WORD_MULTI_WORD in errors -> R.string.mute_rule_error_whole_word
        MuteRuleValidationError.EMPTY_TAB_SELECTION in errors -> R.string.mute_rule_error_no_tabs
        MuteRuleValidationError.DUPLICATE_RULE in errors -> R.string.mute_rule_error_duplicate
        else -> return null
    }
    return stringResource(res)
}
