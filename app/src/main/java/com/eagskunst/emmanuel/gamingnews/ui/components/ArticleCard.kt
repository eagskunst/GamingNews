package com.eagskunst.emmanuel.gamingnews.ui.components

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(16.dp)
private val ImageShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

enum class ArticleMenuAction {
    OPEN_CUSTOM_TAB,
    OPEN_EXTERNAL_BROWSER,
    OPEN_READER_MODE,
    SHARE,
    TOGGLE_SAVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    article: NewsArticle,
    isSaved: Boolean,
    loadImages: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    onMenuAction: (ArticleMenuAction) -> Unit
) {
    var imageFailedToLoad by remember(article.imageUrl) { mutableStateOf(false) }
    val showImage = loadImages && !article.imageUrl.isNullOrBlank() && !imageFailedToLoad
    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showMenu) {
        ArticleActionsBottomSheet(
            sheetState = sheetState,
            isSaved = isSaved,
            onDismiss = { showMenu = false },
            onAction = { action ->
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showMenu = false
                    onMenuAction(action)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (showImage) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(ImageShape),
                contentScale = ContentScale.Crop,
                onError = {
                    Log.e("ArticleCard", "Error loading image", it.result.throwable)
                    imageFailedToLoad = true
                }
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(article.sourceName)
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(" · ${article.timeAgo()}")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save article",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleActionsBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onAction: (ArticleMenuAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            MenuListItem(
                label = stringResource(R.string.article_open_custom_tab),
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = { onAction(ArticleMenuAction.OPEN_CUSTOM_TAB) }
            )
            MenuListItem(
                label = stringResource(R.string.article_open_external_browser),
                icon = Icons.Default.OpenInBrowser,
                onClick = { onAction(ArticleMenuAction.OPEN_EXTERNAL_BROWSER) }
            )
            MenuListItem(
                label = stringResource(R.string.article_open_reader_mode),
                icon = Icons.Default.Book,
                onClick = { onAction(ArticleMenuAction.OPEN_READER_MODE) }
            )
            MenuListItem(
                label = stringResource(R.string.article_share),
                icon = Icons.Default.Share,
                onClick = { onAction(ArticleMenuAction.SHARE) }
            )
            MenuListItem(
                label = stringResource(if (isSaved) R.string.article_remove_save else R.string.article_save),
                icon = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                onClick = { onAction(ArticleMenuAction.TOGGLE_SAVE) }
            )
        }
    }
}

@Composable
private fun MenuListItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun NewsArticle.timeAgo(): CharSequence = DateUtils.getRelativeTimeSpanString(
    publicationDate.time,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS
)

fun handleArticleMenuAction(
    article: NewsArticle,
    action: ArticleMenuAction,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (String) -> Unit,
    onToggleSave: () -> Unit
) {
    when (action) {
        ArticleMenuAction.OPEN_CUSTOM_TAB -> onOpenArticleWithMode(article.link, ArticleOpenMode.CUSTOM_TAB)
        ArticleMenuAction.OPEN_EXTERNAL_BROWSER -> onOpenArticleWithMode(article.link, ArticleOpenMode.EXTERNAL_BROWSER)
        ArticleMenuAction.OPEN_READER_MODE -> onOpenArticleWithMode(article.link, ArticleOpenMode.READER_MODE)
        ArticleMenuAction.SHARE -> onShareArticle(article.link)
        ArticleMenuAction.TOGGLE_SAVE -> onToggleSave()
    }
}
