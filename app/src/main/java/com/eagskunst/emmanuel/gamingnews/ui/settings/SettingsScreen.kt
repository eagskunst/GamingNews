package com.eagskunst.emmanuel.gamingnews.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.BuildConfig
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.common.ContactInfo
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.ui.components.TopicChip

private const val SHOW_NOTIFICATION_TOPICS = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onContactEmailClick: () -> Unit,
    onContactWebsiteClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        SettingsContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            onDarkThemeChange = viewModel::toggleDarkTheme,
            onLoadImagesChange = viewModel::toggleLoadImages,
            onDailyReminderChange = viewModel::toggleDailyReminder,
            onArticleOpenModeChange = viewModel::setArticleOpenMode,
            onAddTopic = viewModel::addTopic,
            onRemoveTopic = viewModel::removeTopic,
            onContactEmailClick = onContactEmailClick,
            onContactWebsiteClick = onContactWebsiteClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onDarkThemeChange: (Boolean) -> Unit = {},
    onLoadImagesChange: (Boolean) -> Unit = {},
    onDailyReminderChange: (Boolean) -> Unit = {},
    onArticleOpenModeChange: (ArticleOpenMode) -> Unit = {},
    onAddTopic: (String) -> Unit = {},
    onRemoveTopic: (Topic) -> Unit = {},
    onContactEmailClick: () -> Unit = {},
    onContactWebsiteClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        PreferenceSwitch(
            title = "Dark theme",
            checked = uiState.darkTheme,
            onCheckedChange = onDarkThemeChange
        )
        PreferenceSwitch(
            title = "Load images",
            checked = uiState.loadImages,
            onCheckedChange = onLoadImagesChange
        )
        PreferenceSwitch(
            title = "Daily reminder",
            checked = uiState.dailyReminder,
            onCheckedChange = onDailyReminderChange
        )
        ArticleOpenModeRow(
            selectedMode = uiState.articleOpenMode,
            onModeSelected = onArticleOpenModeChange
        )

        if (SHOW_NOTIFICATION_TOPICS) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Notification topics",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.topics.forEach { topic ->
                    TopicChip(
                        topic = topic,
                        onRemove = { onRemoveTopic(topic) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            var showDialog by remember { mutableStateOf(false) }
            Button(onClick = { showDialog = true }) {
                Text("Add topic")
            }

            if (showDialog) {
                AddTopicDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { name ->
                        onAddTopic(name)
                        showDialog = false
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        AboutAndContactSection(
            onContactEmailClick = onContactEmailClick,
            onContactWebsiteClick = onContactWebsiteClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick
        )
    }
}

@Composable
private fun AboutAndContactSection(
    onContactEmailClick: () -> Unit,
    onContactWebsiteClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.about_and_contact),
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.about_app_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    LinkRow(
        icon = Icons.Filled.Email,
        title = stringResource(R.string.contact_email_title),
        subtitle = ContactInfo.EMAIL,
        onClick = onContactEmailClick
    )
    LinkRow(
        icon = Icons.Filled.Language,
        title = stringResource(R.string.contact_website_title),
        subtitle = stringResource(R.string.contact_website_url),
        onClick = onContactWebsiteClick
    )
    LinkRow(
        icon = Icons.Filled.PrivacyTip,
        title = stringResource(R.string.privacy_policy_title),
        subtitle = stringResource(R.string.privacy_policy_url),
        onClick = onPrivacyPolicyClick
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.developer_name),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArticleOpenModeRow(
    selectedMode: ArticleOpenMode,
    onModeSelected: (ArticleOpenMode) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val modeLabel = stringResource(
        when (selectedMode) {
            ArticleOpenMode.CUSTOM_TAB -> R.string.article_open_custom_tab
            ArticleOpenMode.EXTERNAL_BROWSER -> R.string.article_open_external_browser
            ArticleOpenMode.READER_MODE -> R.string.article_open_custom_tab
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp)
    ) {
        Text(text = stringResource(R.string.settings_article_open_mode), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = modeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_article_open_mode)) },
            text = {
                Column {
                    ArticleOpenMode.entries.filter { it != ArticleOpenMode.READER_MODE }.forEach { mode ->
                        val label = stringResource(
                            when (mode) {
                                ArticleOpenMode.CUSTOM_TAB -> R.string.article_open_custom_tab
                                ArticleOpenMode.EXTERNAL_BROWSER -> R.string.article_open_external_browser
                                ArticleOpenMode.READER_MODE -> R.string.article_open_custom_tab
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onModeSelected(mode)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, modifier = Modifier.weight(1f))
                            if (mode == selectedMode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AddTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add notification topic") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Topic") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
