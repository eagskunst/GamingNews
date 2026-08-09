package com.eagskunst.emmanuel.gamingnews.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.ui.components.ArticleCard
import com.eagskunst.emmanuel.gamingnews.ui.components.MainTopAppBar

private val categories = NewsCategory.entries.toTypedArray()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onSettingsClick: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredArticles = remember(uiState.articles, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.articles
        } else {
            uiState.articles.filter { it.title.contains(uiState.searchQuery, ignoreCase = true) }
        }
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

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredArticles, key = { it.link }) { article ->
                    ArticleCard(
                        article = article,
                        isSaved = uiState.savedLinks.contains(article.link),
                        onToggleSave = { viewModel.toggleSavedArticle(article) },
                        onClick = { onOpenArticle(article.link) }
                    )
                }
            }
        }
    }
}

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
