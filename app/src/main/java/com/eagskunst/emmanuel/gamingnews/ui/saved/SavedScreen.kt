package com.eagskunst.emmanuel.gamingnews.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleCard
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleMenuAction
import com.eagskunst.emmanuel.gamingnews.ui.components.MainTopAppBar
import com.eagskunst.emmanuel.gamingnews.ui.components.handleArticleMenuAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
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
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Scaffold(
        // The outer Scaffold in MainActivity already reserves space for the bottom
        // navigation bar; only consume the top status bar inset here to avoid double padding.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.saved),
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        if (filteredArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_saved_articles),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredArticles, key = { it.link }) { article ->
                    ArticleCard(
                        article = article,
                        isSaved = true,
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
    }
}
