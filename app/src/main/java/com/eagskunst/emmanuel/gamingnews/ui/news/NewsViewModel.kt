package com.eagskunst.emmanuel.gamingnews.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteContext
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
    val loadImages: Boolean = true,
    val newArticlesCount: Int? = null,
    val mutedCount: Int = 0,
    val isMutedRevealed: Boolean = false,
    val revealedMutedLinks: Set<String> = emptySet(),
    val feedEmptyState: FilteredFeed.EmptyState = FilteredFeed.EmptyState.NONE,
    val sourceLinks: Set<String> = emptySet()
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

    private val searchQueryFlow = MutableStateFlow("")
    private val revealMutedFlow = MutableStateFlow(false)

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
        revealMutedFlow.value = false
        _uiState.update { it.copy(selectedCategory = category, isMutedRevealed = false) }
        refresh(forceRefresh = true)
    }

    fun refresh(forceRefresh: Boolean = true, notifyNewArticles: Boolean = false) {
        refreshJob?.cancel()
        val previousLinks = _uiState.value.sourceLinks
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val urls = getFeedUrlsUseCase(_uiState.value.selectedCategory)
            // Count new articles only on the first emission of this refresh: later emissions
            // come from rule/search/reveal changes re-filtering the same snapshot and must
            // not re-trigger or resurrect the banner.
            var countedNewArticles = false
            getNewsUseCase(
                urls = urls,
                forceRefresh = forceRefresh,
                context = MuteContext.NewsTab(_uiState.value.selectedCategory),
                searchQuery = searchQueryFlow,
                revealMuted = revealMutedFlow
            ).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> {
                        val feed = result.data
                        val countNow = !countedNewArticles
                        countedNewArticles = true
                        _uiState.update {
                            it.copy(
                                articles = feed.articles,
                                isLoading = false,
                                errorMessage = null,
                                newArticlesCount = if (countNow) {
                                    if (notifyNewArticles) {
                                        feed.sourceLinks
                                            .count { link -> link !in previousLinks }
                                            .takeIf { count -> count > 0 }
                                    } else {
                                        null
                                    }
                                } else {
                                    it.newArticlesCount
                                },
                                mutedCount = feed.mutedCount,
                                isMutedRevealed = feed.isRevealed,
                                revealedMutedLinks = feed.revealedMutedLinks,
                                feedEmptyState = feed.emptyState,
                                sourceLinks = feed.sourceLinks
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exception.localizedMessage)
                    }
                }
            }
        }
    }

    fun dismissNewArticlesBanner() {
        _uiState.update { it.copy(newArticlesCount = null) }
    }

    fun toggleSavedArticle(article: NewsArticle) {
        viewModelScope.launch { toggleSavedArticleUseCase(article) }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleMutedReveal() {
        revealMutedFlow.update { !it }
    }

    /** Resets the temporary reveal, e.g. when leaving the News tab. */
    fun resetMutedReveal() {
        revealMutedFlow.value = false
    }
}
