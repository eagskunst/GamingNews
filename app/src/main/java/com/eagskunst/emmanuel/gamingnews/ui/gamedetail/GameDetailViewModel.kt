package com.eagskunst.emmanuel.gamingnews.ui.gamedetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.model.pendingRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.GameDetailsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.FollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveFollowedGameIdsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UnfollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesUiEvent
import com.eagskunst.emmanuel.gamingnews.utility.canPostNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GameDetailUiState(
    val details: GameDetails? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val loadImages: Boolean = true,
    val isFollowed: Boolean = false,
    /** The release a follow would track; null hides the Follow button. */
    val pendingRelease: GameRelease? = null,
    val todayEpochDay: Long = 0,
    val showOffsetPicker: Boolean = false
)

/**
 * Drives the game detail page. Follow events reuse [ReleasesUiEvent] because the permission,
 * picker and Undo flow is the same one the Releases bell runs.
 */
@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val repository: GameDetailsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val observeFollowedGameIds: ObserveFollowedGameIdsUseCase,
    private val followGame: FollowGameUseCase,
    private val unfollowGame: UnfollowGameUseCase,
    private val catalog: PlatformCatalog
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle[GAME_ID_ARG])

    private val _uiState = MutableStateFlow(
        GameDetailUiState(todayEpochDay = LocalDate.now().toEpochDay())
    )
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ReleasesUiEvent>()
    val uiEvent: SharedFlow<ReleasesUiEvent> = _uiEvent.asSharedFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                _uiState.update { it.copy(loadImages = preferences.loadImages) }
            }
        }
        viewModelScope.launch {
            observeFollowedGameIds().collect { ids ->
                _uiState.update { it.copy(isFollowed = gameId in ids) }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.gameDetails(gameId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update {
                        it.copy(isLoading = true, hasError = false)
                    }
                    is Result.Success -> _uiState.update {
                        it.copy(
                            details = result.data,
                            pendingRelease = result.data.pendingRelease(
                                it.todayEpochDay,
                                catalog.supportedIds()
                            ),
                            isLoading = false,
                            hasError = false
                        )
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, hasError = true)
                    }
                }
            }
        }
    }

    fun retry() = load()

    /**
     * Follow button tap: an existing follow is removed (the snackbar offers Undo); a new one
     * needs notification permission first, then opens the reminder picker.
     */
    fun onFollowClick() {
        val state = _uiState.value
        if (state.pendingRelease == null) return
        if (state.isFollowed) {
            viewModelScope.launch {
                unfollowGame(gameId)?.let { removed ->
                    _uiEvent.emit(ReleasesUiEvent.ShowReminderRemoved(removed))
                }
            }
            return
        }
        if (context.canPostNotifications()) {
            _uiState.update { it.copy(showOffsetPicker = true) }
        } else {
            viewModelScope.launch {
                _uiEvent.emit(ReleasesUiEvent.RequestNotificationPermission)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _uiState.update { it.copy(showOffsetPicker = it.pendingRelease != null) }
        } else {
            viewModelScope.launch { _uiEvent.emit(ReleasesUiEvent.ShowPermissionDenied) }
        }
    }

    fun onOffsetsConfirmed(offsets: Set<ReminderOffset>) {
        val release = _uiState.value.pendingRelease ?: return
        _uiState.update { it.copy(showOffsetPicker = false) }
        viewModelScope.launch { followGame(release, offsets) }
    }

    fun onPickerDismissed() {
        _uiState.update { it.copy(showOffsetPicker = false) }
    }

    /** Re-follows a game that was just unfollowed via the Undo snackbar. */
    fun undoUnfollow(game: FollowedGame) {
        viewModelScope.launch { followGame.restore(game) }
    }

    companion object {
        const val GAME_ID_ARG = "gameId"
    }
}
