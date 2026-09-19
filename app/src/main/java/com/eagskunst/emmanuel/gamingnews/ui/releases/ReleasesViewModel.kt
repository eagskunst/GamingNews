package com.eagskunst.emmanuel.gamingnews.ui.releases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRejectedTokenException
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbTokenAcquisitionException
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReleasesError {
    TOKEN_ACQUISITION,
    IGDB_REJECTION,
    REFRESH,
    PAGINATION
}

data class ReleasesUiState(
    val releases: List<GameRelease> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val error: ReleasesError? = null
)

@HiltViewModel
class ReleasesViewModel @Inject constructor(
    private val repository: ReleasesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReleasesUiState())
    val uiState: StateFlow<ReleasesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.hasMorePages.collect { hasMorePages ->
                _uiState.update { it.copy(hasMorePages = hasMorePages) }
            }
        }
        refresh()
    }

    fun refresh() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.releasesStream(forceRefresh = true).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(releases = result.data, isLoading = false, error = null)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.exception.toReleasesError(false))
                    }
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            when (val result = repository.loadNextPage()) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = result.exception.toReleasesError(true)
                        )
                    }
                }
                else -> _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

private fun Throwable.toReleasesError(isPagination: Boolean): ReleasesError = when (this) {
    is IgdbTokenAcquisitionException -> ReleasesError.TOKEN_ACQUISITION
    is IgdbRejectedTokenException -> ReleasesError.IGDB_REJECTION
    else -> if (isPagination) ReleasesError.PAGINATION else ReleasesError.REFRESH
}
