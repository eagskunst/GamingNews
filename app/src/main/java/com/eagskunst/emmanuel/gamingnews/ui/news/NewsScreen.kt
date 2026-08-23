package com.eagskunst.emmanuel.gamingnews.ui.news

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleCard
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleMenuAction
import com.eagskunst.emmanuel.gamingnews.ui.components.MainTopAppBar
import com.eagskunst.emmanuel.gamingnews.ui.components.handleArticleMenuAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val categories = NewsCategory.entries.toTypedArray()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onSettingsClick: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (String) -> Unit,
    scrollToTopSignal: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredArticles = remember(uiState.articles, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.articles
        } else {
            uiState.articles.filter { it.title.contains(uiState.searchQuery, ignoreCase = true) }
        }
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }
    val onNewArticlesBannerClick = {
        coroutineScope.launch { listState.animateScrollToItem(0) }
        viewModel.dismissNewArticlesBanner()
    }

    Scaffold(
        // The outer Scaffold in MainActivity already reserves space for the bottom
        // navigation bar; only consume the top status bar inset here to avoid double padding.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.app_name),
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
            CategorySelector(
                selected = uiState.selectedCategory,
                onSelected = viewModel::selectCategory,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.refresh(forceRefresh = true, notifyNewArticles = true) },
                    modifier = Modifier.fillMaxSize()
                ) {

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredArticles, key = { it.link }) { article ->
                            ArticleCard(
                                article = article,
                                isSaved = uiState.savedLinks.contains(article.link),
                                loadImages = uiState.loadImages,
                                onToggleSave = { viewModel.toggleSavedArticle(article) },
                                onClick = { onOpenArticle(article.link) },
                                onMenuAction = { action ->
                                    handleArticleMenuAction(
                                        article = article,
                                        action = action,
                                        onOpenArticleWithMode = onOpenArticleWithMode,
                                        onShareArticle = onShareArticle,
                                        onToggleSave = { viewModel.toggleSavedArticle(article) }
                                    )
                                }
                            )
                        }
                    }
                }

                NewArticlesBanner(
                    newArticlesCount = uiState.newArticlesCount,
                    onClick = onNewArticlesBannerClick,
                    onDismiss = viewModel::dismissNewArticlesBanner,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun NewArticlesBanner(
    newArticlesCount: Int?,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = newArticlesCount != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        if (newArticlesCount != null) {
            LaunchedEffect(newArticlesCount) {
                delay(NEW_ARTICLES_BANNER_DURATION_MS)
                onDismiss()
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = 4.dp,
                onClick = {
                    onClick()
                    onDismiss()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.new_articles_available,
                            newArticlesCount,
                            newArticlesCount
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

private const val NEW_ARTICLES_BANNER_DURATION_MS = 3000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    selected: NewsCategory,
    onSelected: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(category) },
                label = { Text(category.displayName()) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun NewsCategory.displayName(): String = when (this) {
    NewsCategory.ALL -> "All"
    NewsCategory.SONY -> "Playstation"
    NewsCategory.MICROSOFT -> "Xbox"
    NewsCategory.NINTENDO -> "Nintendo"
    NewsCategory.PC -> "PC"
}
