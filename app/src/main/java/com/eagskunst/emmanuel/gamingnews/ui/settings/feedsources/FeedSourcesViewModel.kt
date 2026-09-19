package com.eagskunst.emmanuel.gamingnews.ui.settings.feedsources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetFeedProvidersUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RestoreFeedProvidersDefaultsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SetFeedProviderEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedSourcesUiState(
    val selectedCategory: NewsCategory = NewsCategory.ALL,
    val providers: List<FeedProvider> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FeedSourcesViewModel @Inject constructor(
    private val getFeedProvidersUseCase: GetFeedProvidersUseCase,
    private val setFeedProviderEnabledUseCase: SetFeedProviderEnabledUseCase,
    private val restoreFeedProvidersDefaultsUseCase: RestoreFeedProvidersDefaultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedSourcesUiState())
    val uiState: StateFlow<FeedSourcesUiState> = _uiState.asStateFlow()

    init {
        observeProviders()
    }

    fun selectCategory(category: NewsCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        observeProviders()
    }

    fun toggleProvider(id: String, enabled: Boolean) {
        viewModelScope.launch {
            setFeedProviderEnabledUseCase(id, enabled)
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            restoreFeedProvidersDefaultsUseCase()
        }
    }

    private fun observeProviders() {
        viewModelScope.launch {
            getFeedProvidersUseCase(_uiState.value.selectedCategory).collect { providers ->
                _uiState.update {
                    it.copy(providers = providers, isLoading = false)
                }
            }
        }
    }
}
