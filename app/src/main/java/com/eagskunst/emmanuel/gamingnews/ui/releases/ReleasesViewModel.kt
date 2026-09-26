package com.eagskunst.emmanuel.gamingnews.ui.releases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRejectedTokenException
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbTokenAcquisitionException
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GamePlatform
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionNotice
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseFilters
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

enum class ReleasesError {
    TOKEN_ACQUISITION,
    IGDB_REJECTION,
    REFRESH,
    PAGINATION,
    SELECTION_SAVE
}

data class ReleasesUiState(
    val releases: List<GameRelease> = emptyList(),
    val matchCount: Int = 0,
    val searchQuery: String = "",
    val platformOptions: List<GamePlatform> = emptyList(),
    val selectedPlatformIds: Set<Int> = emptySet(),
    val unknownPlatformIds: Set<Int> = emptySet(),
    val catalogNotice: PlatformSelectionNotice? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val error: ReleasesError? = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedPlatformIds.isNotEmpty()

    val isBusy: Boolean
        get() = isLoading || isRefreshing || isLoadingMore
}

@HiltViewModel
class ReleasesViewModel @Inject constructor(
    private val getReleases: GetReleasesUseCase,
    private val releasesRepository: ReleasesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    catalog: PlatformCatalog
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReleasesUiState(platformOptions = catalog.supportedPlatforms())
    )
    val uiState: StateFlow<ReleasesUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    // Null until the persisted selection arrives — keeps results unsettled (and the network
    // fetch un-started) until preferences are restored, avoiding an unfiltered flash.
    private val selectedPlatformIds = MutableStateFlow<Set<Int>?>(null)
    private val selectionMutex = Mutex()

    init {
        observePlatformSelection()
        observeReleases()
        observeHasMorePages()
        autoLoadMatchingPages()
    }

    private fun observePlatformSelection() = viewModelScope.launch {
        userPreferencesRepository.releasePlatformSelection.collect { selection ->
            selectedPlatformIds.value = selection.selectedIds
            _uiState.update {
                it.copy(
                    selectedPlatformIds = selection.selectedIds,
                    unknownPlatformIds = selection.unknownIds,
                    catalogNotice = selection.notice ?: it.catalogNotice
                )
            }
        }
    }

    private fun observeReleases() = viewModelScope.launch {
        val filters = combine(searchQuery, selectedPlatformIds) { query, selection ->
            selection?.let { ReleaseFilters(platformIds = it, searchQuery = query) }
        }.filterNotNull()
        getReleases(filters).collect { result ->
            when (result) {
                is Result.Loading -> _uiState.update {
                    it.copy(isLoading = true, error = it.error.persistedSelectionError())
                }
                is Result.Success -> _uiState.update {
                    it.copy(
                        releases = result.data.releases,
                        matchCount = result.data.matchCount,
                        isLoading = false,
                        error = it.error.persistedSelectionError()
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.toReleasesError(false))
                }
            }
        }
    }

    private fun observeHasMorePages() = viewModelScope.launch {
        releasesRepository.hasMorePages.collect { hasMorePages ->
            _uiState.update { it.copy(hasMorePages = hasMorePages) }
        }
    }

    /**
     * Keeps fetching pages while the filtered result can't fill the viewport — including zero
     * matches — so unmatched early pages can't hide later matches or fake an empty state.
     * Each iteration reads the live state, so conflated emissions still terminate correctly
     * on exhaustion or failure; [retry] resumes after an error.
     */
    private fun autoLoadMatchingPages() = viewModelScope.launch {
        uiState.collect {
            while (shouldAutoLoad(_uiState.value)) {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
                when (val result = releasesRepository.loadNextPage()) {
                    is Result.Error -> _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = result.exception.toReleasesError(true)
                        )
                    }
                    else -> _uiState.update {
                        // Sync immediately: the hasMorePages collector can't interleave while
                        // this loop is draining, so exhaustion must be read from the repo.
                        it.copy(
                            isLoadingMore = false,
                            hasMorePages = releasesRepository.hasMorePages.value
                        )
                    }
                }
                // Let the releases/hasMorePages collectors publish fresh state before the
                // next iteration, so newly cached matches stop the drain early.
                yield()
            }
        }
    }

    private fun shouldAutoLoad(state: ReleasesUiState): Boolean =
        state.releases.size < MIN_PREFETCHED_MATCHES &&
            state.hasMorePages &&
            !state.isLoading &&
            !state.isRefreshing &&
            !state.isLoadingMore &&
            state.error == null

    fun refresh() {
        val state = _uiState.value
        if (state.isBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            when (val result = releasesRepository.refresh()) {
                is Result.Error -> _uiState.update {
                    it.copy(isRefreshing = false, error = result.exception.toReleasesError(false))
                }
                else -> _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            when (val result = releasesRepository.loadNextPage()) {
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

    fun retry() {
        val error = _uiState.value.error ?: return
        _uiState.update { it.copy(error = null) }
        if (error == ReleasesError.PAGINATION) loadMore() else refresh()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onPlatformToggle(platformId: Int) {
        val current = selectedPlatformIds.value ?: return
        val next = if (platformId in current) current - platformId else current + platformId
        persistPlatformSelection(next)
    }

    fun onSelectAllPlatforms() = persistPlatformSelection(emptySet())

    fun clearFilters() {
        onSearchQueryChange("")
        persistPlatformSelection(emptySet())
    }

    fun dismissCatalogNotice() {
        _uiState.update { it.copy(catalogNotice = null) }
    }

    private fun persistPlatformSelection(ids: Set<Int>) {
        viewModelScope.launch {
            selectionMutex.withLock {
                selectedPlatformIds.value = ids
                _uiState.update {
                    it.copy(
                        selectedPlatformIds = ids,
                        unknownPlatformIds = it.unknownPlatformIds.intersect(ids)
                    )
                }
                try {
                    userPreferencesRepository.updateReleasePlatformIds(ids)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = ReleasesError.SELECTION_SAVE) }
                }
            }
        }
    }

    private companion object {
        const val MIN_PREFETCHED_MATCHES = 6
    }
}

// Stream emissions only own fetch-related errors; a pending selection-save failure belongs
// to the write that produced it and must survive later data emissions.
private fun ReleasesError?.persistedSelectionError(): ReleasesError? =
    takeIf { it == ReleasesError.SELECTION_SAVE }

private fun Throwable.toReleasesError(isPagination: Boolean): ReleasesError = when (this) {
    is IgdbTokenAcquisitionException -> ReleasesError.TOKEN_ACQUISITION
    is IgdbRejectedTokenException -> ReleasesError.IGDB_REJECTION
    else -> if (isPagination) ReleasesError.PAGINATION else ReleasesError.REFRESH
}
