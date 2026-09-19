package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReaderArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val getReaderArticleUseCase: GetReaderArticleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val articleUrl: String = checkNotNull(savedStateHandle.get<String>(ReaderActivity.EXTRA_URL)) {
        "ReaderActivity started without article URL"
    }

    private val userPreferences = getUserPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val themeMode: StateFlow<ThemeMode> = userPreferences
        .map { it?.themeMode ?: ThemeMode.SYSTEM }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val dynamicColor: StateFlow<Boolean> = userPreferences
        .map { it?.dynamicColor ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val darkThemeEnabled: StateFlow<Boolean> = userPreferences
        .map { it?.themeMode == ThemeMode.DARK }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val loadImages: StateFlow<Boolean> = userPreferences
        .map { it?.loadImages ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fetchArticle()
        }
    }

    private suspend fun fetchArticle() {
        _uiState.value = ReaderUiState.Loading
        _uiState.value = try {
            val article = getReaderArticleUseCase(articleUrl)
            if (article != null) {
                ReaderUiState.Content(article)
            } else {
                ReaderUiState.Error
            }
        } catch (e: Exception) {
            ReaderUiState.Error
        }
    }

    fun retry() {
        viewModelScope.launch { fetchArticle() }
    }
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data object Error : ReaderUiState
    data class Content(val article: com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle) : ReaderUiState
}
