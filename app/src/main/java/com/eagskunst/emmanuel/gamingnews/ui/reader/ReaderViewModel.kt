package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    val darkThemeEnabled: StateFlow<Boolean> = getUserPreferencesUseCase()
        .map { it.darkTheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
}
