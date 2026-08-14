package com.eagskunst.emmanuel.gamingnews.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetFeedUrlsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetNewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val articles: List<NewsArticle> = emptyList(),
    val savedLinks: Set<String> = emptySet(),
    val selectedCategory: NewsCategory = NewsCategory.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loadImages: Boolean = true
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val getSavedArticlesUseCase: GetSavedArticlesUseCase,
    private val toggleSavedArticleUseCase: ToggleSavedArticleUseCase,
    private val getFeedUrlsUseCase: GetFeedUrlsUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            getSavedArticlesUseCase().collect { saved ->
                _uiState.update { it.copy(savedLinks = saved.map { article -> article.link }.toSet()) }
            }
        }
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { preferences ->
                _uiState.update { it.copy(loadImages = preferences.loadImages) }
            }
        }
        refresh(forceRefresh = false)
    }

    fun selectCategory(category: NewsCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        refresh(forceRefresh = true)
    }

    fun refresh(forceRefresh: Boolean = true) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val urls = getFeedUrlsUseCase(_uiState.value.selectedCategory)
            getNewsUseCase(urls, forceRefresh).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(articles = result.data, isLoading = false, errorMessage = null)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exception.localizedMessage)
                    }
                }
            }
        }
    }


    fun toggleSavedArticle(article: NewsArticle) {
        viewModelScope.launch { toggleSavedArticleUseCase(article) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
