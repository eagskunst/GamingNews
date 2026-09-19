package com.eagskunst.emmanuel.gamingnews.ui.reviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.ui.components.AllMutedEmptyState
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleMenuAction
import com.eagskunst.emmanuel.gamingnews.ui.components.MainTopAppBar
import com.eagskunst.emmanuel.gamingnews.ui.components.MutedArticlesNotice
import com.eagskunst.emmanuel.gamingnews.ui.components.rememberMutedNoticeScrollBehavior
import com.eagskunst.emmanuel.gamingnews.ui.components.ReviewArticleCard
import com.eagskunst.emmanuel.gamingnews.ui.components.handleArticleMenuAction
import com.eagskunst.emmanuel.gamingnews.utility.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel,
    onSettingsClick: () -> Unit,
    onManageMutedWords: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (String) -> Unit,
    scrollToTopSignal: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val mutedNoticeBehavior = rememberMutedNoticeScrollBehavior(resetKey = uiState.mutedCount)

    // Reset the temporary muted reveal when this destination leaves composition (tab switch),
    // but not when the activity is only recreating for a configuration change.
    val context = LocalContext.current
    DisposableEffect(Unit) {
        onDispose {
            if (context.findActivity()?.isChangingConfigurations != true) {
                viewModel.resetMutedReveal()
            }
        }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.reviews),
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = mutedNoticeBehavior.visible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                MutedArticlesNotice(
                    mutedCount = uiState.mutedCount,
                    revealed = uiState.isMutedRevealed,
                    onToggleReveal = viewModel::toggleMutedReveal,
                    onManage = onManageMutedWords
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(mutedNoticeBehavior.nestedScrollConnection)
            ) {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading && uiState.articles.isNotEmpty(),
                    onRefresh = { viewModel.refresh(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        uiState.isLoading && uiState.articles.isEmpty() -> {
                            LoadingContent(modifier = Modifier.fillMaxSize())
                        }
                        uiState.feedEmptyState == FilteredFeed.EmptyState.ALL_MUTED -> {
                            AllMutedEmptyState(
                                onShowHidden = viewModel::toggleMutedReveal,
                                onManage = onManageMutedWords,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        uiState.articles.isEmpty() -> {
                            EmptyContent(
                                searchActive = uiState.feedEmptyState == FilteredFeed.EmptyState.NO_SEARCH_RESULTS,
                                errorMessage = uiState.errorMessage,
                                onRetry = { viewModel.refresh(forceRefresh = true) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            ReviewsList(
                                articles = uiState.articles,
                                savedLinks = uiState.savedLinks,
                                loadImages = uiState.loadImages,
                                revealedMutedLinks = uiState.revealedMutedLinks,
                                listState = listState,
                                onToggleSave = viewModel::toggleSavedArticle,
                                onOpenArticle = onOpenArticle,
                                onOpenArticleWithMode = onOpenArticleWithMode,
                                onShareArticle = onShareArticle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                uiState.staleSourceMessage?.let {
                    StaleBanner(
                        message = stringResource(R.string.stale_source_message),
                        onDismiss = viewModel::dismissError,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewsList(
    articles: List<NewsArticle>,
    savedLinks: Set<String>,
    loadImages: Boolean,
    revealedMutedLinks: Set<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onToggleSave: (NewsArticle) -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(
            items = articles,
            key = { _, article -> article.link }
        ) { index, article ->
            ReviewArticleCard(
                article = article,
                isSaved = article.link in savedLinks,
                loadImages = loadImages,
                isHero = index == 0,
                isRevealedMuted = article.link in revealedMutedLinks,
                onToggleSave = { onToggleSave(article) },
                onClick = { onOpenArticle(article.link) },
                onMenuAction = { action ->
                    handleArticleMenuAction(
                        article = article,
                        action = action,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle,
                        onToggleSave = { onToggleSave(article) }
                    )
                }
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(
    searchActive: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = when {
                errorMessage != null -> errorMessage
                searchActive -> stringResource(R.string.no_review_search_results)
                else -> stringResource(R.string.no_reviews)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (errorMessage != null) {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun StaleBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Snackbar(
        modifier = modifier.fillMaxWidth(),
        action = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    ) {
        Text(text = message)
    }
}
