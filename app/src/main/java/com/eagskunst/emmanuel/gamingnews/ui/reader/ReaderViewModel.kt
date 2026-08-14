package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val okHttpClient: OkHttpClient,
    private val dispatchers: DispatcherProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val articleUrl: String = checkNotNull(savedStateHandle.get<String>(ReaderActivity.EXTRA_URL)) {
        "ReaderActivity started without article URL"
    }

    val themeMode: StateFlow<ThemeMode> = getUserPreferencesUseCase()
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val dynamicColor: StateFlow<Boolean> = getUserPreferencesUseCase()
        .map { it.dynamicColor }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val darkThemeEnabled: StateFlow<Boolean> = getUserPreferencesUseCase()
        .map { it.themeMode == ThemeMode.DARK }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )


    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fetchArticleHtml()
        }
    }

    private suspend fun fetchArticleHtml() {
        try {
            val html = withContext(dispatchers.io) {
                val request = Request.Builder()
                    .url(articleUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body.string().takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }
            if (html != null) {
                _uiState.value = ReaderUiState.Content(articleUrl, html)
            } else {
                _uiState.value = ReaderUiState.Error
            }
        } catch (e: Exception) {
            _uiState.value = ReaderUiState.Error
        }
    }

    fun retry() {
        _uiState.value = ReaderUiState.Loading
        viewModelScope.launch { fetchArticleHtml() }
    }
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Content(val url: String, val html: String) : ReaderUiState
    data object Error : ReaderUiState
}
