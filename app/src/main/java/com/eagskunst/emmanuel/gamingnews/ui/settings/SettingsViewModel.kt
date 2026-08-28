package com.eagskunst.emmanuel.gamingnews.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderHourUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDailyReminderUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDarkThemeUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateDynamicColorUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateLoadImagesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateThemeModeUseCase
import com.eagskunst.emmanuel.gamingnews.worker.DailyReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

sealed interface SettingsUiEvent {
    data object RequestNotificationPermission : SettingsUiEvent
    data class ShowMessage(val message: String) : SettingsUiEvent
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val darkTheme: Boolean = false,
    val loadImages: Boolean = true,
    val dailyReminder: Boolean = false,
    val dailyReminderHour: Int = 9,
    val nextReminderLabel: String = "",
    val articleOpenMode: ArticleOpenMode = ArticleOpenMode.READER_MODE,
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
    private val updateDailyReminderHourUseCase: UpdateDailyReminderHourUseCase,
    private val updateArticleOpenModeUseCase: UpdateArticleOpenModeUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val removeTopicUseCase: RemoveTopicUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()

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
            dailyReminderHour = preferences.dailyReminderHour,
            nextReminderLabel = computeNextReminderLabel(preferences.dailyReminderHour),
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
        if (enabled && !canPostNotifications()) {
            viewModelScope.launch {
                _uiEvent.emit(SettingsUiEvent.RequestNotificationPermission)
            }
            return
        }
        applyDailyReminder(enabled)
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            applyDailyReminder(true)
        } else {
            viewModelScope.launch {
                _uiEvent.emit(
                    SettingsUiEvent.ShowMessage(
                        context.getString(R.string.notification_permission_rationale)
                    )
                )
            }
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            updateDailyReminderHourUseCase(hour)
            if (uiState.value.dailyReminder) {
                DailyReminderScheduler.schedule(context, hour)
            }
        }
    }

    private fun applyDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            updateDailyReminderUseCase(enabled)
            if (enabled) {
                DailyReminderScheduler.schedule(context, uiState.value.dailyReminderHour)
            } else {
                DailyReminderScheduler.cancel(context)
            }
        }
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun computeNextReminderLabel(hour: Int): String {
        val current = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(current)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return String.format(Locale.getDefault(), "%02d:00", target.get(Calendar.HOUR_OF_DAY))
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

