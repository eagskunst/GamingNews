package com.eagskunst.emmanuel.gamingnews.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkTheme: Boolean = false,
    val loadImages: Boolean = true,
    val dailyReminder: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    getTopicsUseCase: GetTopicsUseCase,
    private val updateDarkThemeUseCase: UpdateDarkThemeUseCase,
    private val updateLoadImagesUseCase: UpdateLoadImagesUseCase,
    private val updateDailyReminderUseCase: UpdateDailyReminderUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val removeTopicUseCase: RemoveTopicUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        getUserPreferencesUseCase(),
        getTopicsUseCase()
    ) { preferences, topics ->
        SettingsUiState(
            darkTheme = preferences.darkTheme,
            loadImages = preferences.loadImages,
            dailyReminder = preferences.dailyReminder,
            topics = topics,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch { updateDarkThemeUseCase(enabled) }
    }

    fun toggleLoadImages(enabled: Boolean) {
        viewModelScope.launch { updateLoadImagesUseCase(enabled) }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch { updateDailyReminderUseCase(enabled) }
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
}
