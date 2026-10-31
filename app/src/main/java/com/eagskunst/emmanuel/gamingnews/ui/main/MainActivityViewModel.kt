package com.eagskunst.emmanuel.gamingnews.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase

import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.ui.ArticleOpener
import com.eagskunst.emmanuel.gamingnews.ui.InAppReviewPrompter
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
    private val articleOpener: ArticleOpener,
    private val reviewPrompter: InAppReviewPrompter,
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
            initialValue = ArticleOpenMode.READER_MODE
        )


    fun openArticle(url: String, mode: ArticleOpenMode) {
        articleOpener(url, mode)
        viewModelScope.launch { reviewPrompter.onArticleOpened() }
    }

    /** Safe to call on every resume; the prompter decides whether to ask. Failures never reach the user. */
    suspend fun maybeRequestReview(activity: android.app.Activity) {
        try {
            reviewPrompter.maybePrompt(activity)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    fun openArticleWithMode(url: String, mode: ArticleOpenMode) {
        viewModelScope.launch { updateArticleOpenModeUseCase(mode) }
        articleOpener(url, mode)
        viewModelScope.launch { reviewPrompter.onArticleOpened() }
    }
}
