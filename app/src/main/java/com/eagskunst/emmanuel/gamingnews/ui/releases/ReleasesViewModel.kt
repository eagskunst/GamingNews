package com.eagskunst.emmanuel.gamingnews.ui.releases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.IgdbRejectedTokenException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.IgdbTokenAcquisitionException
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GamePlatform
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionNotice
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseFilters
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.FollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveFollowedGameIdsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UnfollowGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

sealed interface ReleasesUiEvent {
    /** The screen should launch the POST_NOTIFICATIONS system prompt. */
    data object RequestNotificationPermission : ReleasesUiEvent

    /** The user denied notification permission; explain why the bell can't work. */
    data object ShowPermissionDenied : ReleasesUiEvent

    /** A follow was removed; the screen offers an Undo snackbar for [game]. */
    data class ShowReminderRemoved(val game: FollowedGame) : ReleasesUiEvent
}

data class ReleasesUiState(
    val releases: List<GameRelease> = emptyList(),
    val matchCount: Int = 0,
    val searchQuery: String = "",
    val platformOptions: List<GamePlatform> = emptyList(),
    val selectedPlatformIds: Set<Int> = emptySet(),
    val unknownPlatformIds: Set<Int> = emptySet(),
    val catalogNotice: PlatformSelectionNotice? = null,
    val followedGameIds: Set<Long> = emptySet(),
    val offsetPickerFor: GameRelease? = null,
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
    @ApplicationContext private val context: Context,
    private val getReleases: GetReleasesUseCase,
    private val releasesRepository: ReleasesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    observeFollowedGameIds: ObserveFollowedGameIdsUseCase,
    private val followGame: FollowGameUseCase,
    private val unfollowGame: UnfollowGameUseCase,
    catalog: PlatformCatalog
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReleasesUiState(platformOptions = catalog.supportedPlatforms())
    )
    val uiState: StateFlow<ReleasesUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ReleasesUiEvent>()
    val uiEvent: SharedFlow<ReleasesUiEvent> = _uiEvent.asSharedFlow()

    /** Release whose bell tap is waiting on the permission result before opening the picker. */
    private var pendingFollowRelease: GameRelease? = null

    private val searchQuery = MutableStateFlow("")
    // Null until the persisted selection arrives — keeps results unsettled (and the network
    // fetch un-started) until preferences are restored, avoiding an unfiltered flash.
    private val selectedPlatformIds = MutableStateFlow<Set<Int>?>(null)
    private val selectionMutex = Mutex()
    private val isNearListEnd = MutableStateFlow(false)
    private val pageRequested = MutableStateFlow(false)
    private var paginationFailure: ReleasesError? = null

    init {
        observePlatformSelection()
        observeReleases()
        observeHasMorePages()
        observeFollowedGames(observeFollowedGameIds)
        autoLoadMatchingPages()
    }

    private fun observeFollowedGames(observeFollowedGameIds: ObserveFollowedGameIdsUseCase) =
        viewModelScope.launch {
            observeFollowedGameIds().collect { ids ->
                _uiState.update { it.copy(followedGameIds = ids) }
            }
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
                        error = it.error.persistedSelectionError() ?: paginationFailure
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
        combine(uiState, isNearListEnd, pageRequested) { state, _, _ -> state }.collect {
            while (shouldAutoLoad(_uiState.value)) {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
                pageRequested.value = false
                when (val result = releasesRepository.loadNextPage()) {
                    is Result.Error -> {
                        paginationFailure = result.exception.toReleasesError(true)
                        _uiState.update {
                            it.copy(isLoadingMore = false, error = paginationFailure)
                        }
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
        (state.releases.size < MIN_PREFETCHED_MATCHES || isNearListEnd.value || pageRequested.value) &&
            paginationFailure == null &&
            state.hasMorePages &&
            !state.isLoading &&
            !state.isRefreshing &&
            !state.isLoadingMore &&
            state.error == null

    fun refresh() {
        val state = _uiState.value
        if (state.isBusy) return
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        paginationFailure = null
        viewModelScope.launch {
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
        if (state.isBusy || !state.hasMorePages || state.error != null || paginationFailure != null) return
        pageRequested.value = true
    }

    fun onListEndVisibilityChanged(isVisible: Boolean) {
        isNearListEnd.value = isVisible
    }

    fun retry() {
        val error = _uiState.value.error ?: return
        if (paginationFailure != null || error == ReleasesError.PAGINATION) {
            paginationFailure = null
            pageRequested.value = true
            _uiState.update { it.copy(error = null) }
        } else {
            refresh()
        }
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

    /**
     * Bell tap on a release card. Approximate releases have no bell, but the guard stays so a
     * follow can never be created for a date that isn't day-precision. An already-followed
     * game is unfollowed immediately (the snackbar offers Undo); a new follow first needs
     * notification permission, then opens the offset picker.
     */
    fun onBellClick(release: GameRelease) {
        if (!release.hasExactDate) return
        if (release.id in _uiState.value.followedGameIds) {
            viewModelScope.launch {
                unfollowGame(release.id)?.let { removed ->
                    _uiEvent.emit(ReleasesUiEvent.ShowReminderRemoved(removed))
                }
            }
            return
        }
        if (canPostNotifications()) {
            _uiState.update { it.copy(offsetPickerFor = release) }
        } else {
            pendingFollowRelease = release
            viewModelScope.launch {
                _uiEvent.emit(ReleasesUiEvent.RequestNotificationPermission)
            }
        }
    }

    fun onOffsetsConfirmed(offsets: Set<ReminderOffset>) {
        val release = _uiState.value.offsetPickerFor ?: return
        _uiState.update { it.copy(offsetPickerFor = null) }
        viewModelScope.launch {
            // The picker/bell guards make InexactReleaseDateException unreachable; any other
            // failure leaves the bell unlit, which is honest feedback on its own.
            followGame(release, offsets)
        }
    }

    fun onPickerDismissed() {
        _uiState.update { it.copy(offsetPickerFor = null) }
    }

    fun onPermissionResult(granted: Boolean) {
        val pending = pendingFollowRelease
        pendingFollowRelease = null
        when {
            granted && pending != null -> {
                _uiState.update { it.copy(offsetPickerFor = pending) }
            }
            !granted -> viewModelScope.launch {
                _uiEvent.emit(ReleasesUiEvent.ShowPermissionDenied)
            }
        }
    }

    /** Re-follows a game that was just unfollowed via the bell's Undo snackbar. */
    fun undoUnfollow(game: FollowedGame) {
        viewModelScope.launch { followGame.restore(game) }
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
