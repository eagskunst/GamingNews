package com.eagskunst.emmanuel.gamingnews.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDynamicColorUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateThemeModeUseCase
import com.eagskunst.emmanuel.gamingnews.worker.DailyReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val darkTheme: Boolean = false,
    val loadImages: Boolean = true,
    val dailyReminder: Boolean = false,
    val articleOpenMode: ArticleOpenMode = ArticleOpenMode.CUSTOM_TAB,
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    getTopicsUseCase: GetTopicsUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val updateDynamicColorUseCase: UpdateDynamicColorUseCase,
    private val updateDarkThemeUseCase: UpdateDarkThemeUseCase,
    private val updateLoadImagesUseCase: UpdateLoadImagesUseCase,
    private val updateDailyReminderUseCase: UpdateDailyReminderUseCase,
    private val updateArticleOpenModeUseCase: UpdateArticleOpenModeUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val removeTopicUseCase: RemoveTopicUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        getUserPreferencesUseCase(),
        getTopicsUseCase()
    ) { preferences, topics ->
        SettingsUiState(
            themeMode = preferences.themeMode,
            dynamicColor = preferences.dynamicColor,
            darkTheme = preferences.themeMode == ThemeMode.DARK,
            loadImages = preferences.loadImages,
            dailyReminder = preferences.dailyReminder,
            articleOpenMode = preferences.articleOpenMode,
            topics = topics,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { updateThemeModeUseCase(mode) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { updateDynamicColorUseCase(enabled) }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch { updateDarkThemeUseCase(enabled) }
    }

    fun toggleLoadImages(enabled: Boolean) {
        viewModelScope.launch { updateLoadImagesUseCase(enabled) }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            updateDailyReminderUseCase(enabled)
            if (enabled) {
                DailyReminderScheduler.schedule(context)
            } else {
                DailyReminderScheduler.cancel(context)
            }
        }
    }

    fun addTopic(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch { addTopicUseCase(Topic(trimmed)) }
        }
    }

    fun removeTopic(topic: Topic) {
        viewModelScope.launch { removeTopicUseCase(topic) }
    }

    fun setArticleOpenMode(mode: ArticleOpenMode) {
        viewModelScope.launch { updateArticleOpenModeUseCase(mode) }
    }
}

