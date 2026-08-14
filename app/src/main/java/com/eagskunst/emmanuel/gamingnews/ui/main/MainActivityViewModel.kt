package com.eagskunst.emmanuel.gamingnews.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase

import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.OpenArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val openArticleUseCase: OpenArticleUseCase,
    private val updateArticleOpenModeUseCase: UpdateArticleOpenModeUseCase
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = getUserPreferencesUseCase()
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val dynamicColor: StateFlow<Boolean> = getUserPreferencesUseCase()
        .map { it.dynamicColor }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val darkThemeEnabled: StateFlow<Boolean> = getUserPreferencesUseCase()
        .map { it.themeMode == ThemeMode.DARK }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val articleOpenMode: StateFlow<ArticleOpenMode> = getUserPreferencesUseCase()
        .map { it.articleOpenMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ArticleOpenMode.CUSTOM_TAB
        )


    fun openArticle(url: String, mode: ArticleOpenMode) {
        openArticleUseCase(url, mode)
    }

    fun openArticleWithMode(url: String, mode: ArticleOpenMode) {
        viewModelScope.launch { updateArticleOpenModeUseCase(mode) }
        openArticleUseCase(url, mode)
    }
}
