package com.eagskunst.emmanuel.gamingnews.ui.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredReviewFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReviewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_QUERY_KEY = "reviews_search_query"

data class ReviewsUiState(
    val articles: List<NewsArticle> = emptyList(),
    val savedLinks: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val staleSourceMessage: String? = null,
    val loadImages: Boolean = true,
    val mutedCount: Int = 0,
    val isMutedRevealed: Boolean = false,
    val revealedMutedLinks: Set<String> = emptySet(),
    val feedEmptyState: FilteredFeed.EmptyState = FilteredFeed.EmptyState.NONE
)

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getReviewsUseCase: GetReviewsUseCase,
    private val getSavedArticlesUseCase: GetSavedArticlesUseCase,
    private val toggleSavedArticleUseCase: ToggleSavedArticleUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReviewsUiState(searchQuery = savedStateHandle[SEARCH_QUERY_KEY] ?: "")
    )
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow(_uiState.value.searchQuery)
    private val revealMutedFlow = MutableStateFlow(false)

    private var refreshJob: Job? = null

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

    fun refresh(forceRefresh: Boolean = true) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, staleSourceMessage = null) }
            try {
                getReviewsUseCase(
                    forceRefresh = forceRefresh,
                    searchQuery = searchQueryFlow,
                    revealMuted = revealMutedFlow
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Result.Success -> handleSuccess(result.data)
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.exception.localizedMessage)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    private fun handleSuccess(feed: FilteredReviewFeed) {
        val staleMessage = feed.failedSources.firstOrNull()?.let { failed ->
            "${failed.name}: could not refresh"
        }
        _uiState.update {
            it.copy(
                articles = feed.feed.articles,
                isLoading = false,
                errorMessage = null,
                staleSourceMessage = staleMessage,
                mutedCount = feed.feed.mutedCount,
                isMutedRevealed = feed.feed.isRevealed,
                revealedMutedLinks = feed.feed.revealedMutedLinks,
                feedEmptyState = feed.feed.emptyState
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null, staleSourceMessage = null) }
    }

    fun toggleSavedArticle(article: NewsArticle) {
        viewModelScope.launch {
            try {
                toggleSavedArticleUseCase(article)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = query
        searchQueryFlow.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleMutedReveal() {
        revealMutedFlow.update { !it }
    }

    /** Resets the temporary reveal, e.g. when leaving the Reviews tab. */
    fun resetMutedReveal() {
        revealMutedFlow.value = false
    }
}
