package com.eagskunst.emmanuel.gamingnews.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle

private val CardShape = RoundedCornerShape(16.dp)
private val ThumbnailShape = RoundedCornerShape(12.dp)

@Composable
fun ReviewArticleCard(
    article: NewsArticle,
    isSaved: Boolean,
    loadImages: Boolean,
    isHero: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    onMenuAction: (ArticleMenuAction) -> Unit,
    modifier: Modifier = Modifier,
    isRevealedMuted: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    if (isHero) {
        HeroReviewCard(
            article = article,
            isSaved = isSaved,
            loadImages = loadImages,
            isRevealedMuted = isRevealedMuted,
            onToggleSave = onToggleSave,
            onClick = onClick,
            onShowMenu = { showMenu = true },
            modifier = modifier
        )
    } else {
        CompactReviewCard(
            article = article,
            isSaved = isSaved,
            loadImages = loadImages,
            isRevealedMuted = isRevealedMuted,
            onToggleSave = onToggleSave,
            onClick = onClick,
            onShowMenu = { showMenu = true },
            modifier = modifier
        )
    }

    if (showMenu) {
        ArticleActionsBottomSheet(
            isSaved = isSaved,
            onDismiss = { showMenu = false },
            onAction = { action ->
                showMenu = false
                onMenuAction(action)
            }
        )
    }
}

@Composable
private fun HeroReviewCard(
    article: NewsArticle,
    isSaved: Boolean,
    loadImages: Boolean,
    isRevealedMuted: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageFailed by remember(article.imageUrl) { mutableStateOf(false) }
    val showImage = loadImages && !article.imageUrl.isNullOrBlank() && !imageFailed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
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
                    .aspectRatio(16f / 9f)
                    .clip(ThumbnailShape),
                contentScale = ContentScale.Crop,
                onError = { imageFailed = true }
            )
        }
        ReviewCardContent(
            article = article,
            isSaved = isSaved,
            isHero = true,
            isRevealedMuted = isRevealedMuted,
            onToggleSave = onToggleSave,
            onShowMenu = onShowMenu,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CompactReviewCard(
    article: NewsArticle,
    isSaved: Boolean,
    loadImages: Boolean,
    isRevealedMuted: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageFailed by remember(article.imageUrl) { mutableStateOf(false) }
    val showImage = loadImages && !article.imageUrl.isNullOrBlank() && !imageFailed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewCardContent(
                    article = article,
                    isSaved = isSaved,
                    isHero = false,
                    isRevealedMuted = isRevealedMuted,
                    onToggleSave = onToggleSave,
                    onShowMenu = onShowMenu,
                    modifier = Modifier
                )
            }

            if (showImage) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 96.dp, height = 96.dp)
                        .clip(ThumbnailShape),
                    contentScale = ContentScale.Crop,
                    onError = { imageFailed = true }
                )
            }
        }
    }
}

@Composable
private fun ReviewCardContent(
    article: NewsArticle,
    isSaved: Boolean,
    isHero: Boolean,
    isRevealedMuted: Boolean,
    onToggleSave: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (isRevealedMuted) {
            MutedBadge(modifier = Modifier.padding(bottom = 8.dp))
        }
        Text(
            text = article.title,
            style = if (isHero) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = if (isHero) 3 else 2,
            overflow = TextOverflow.Ellipsis
        )
        if (article.description.isNotBlank()) {
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isHero) 3 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(article.sourceName)
                    }
                    article.author?.takeIf { it.isNotBlank() }?.let { author ->
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(" · $author")
                        }
                    }
                    if (article.publicationDate.time > 0) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(" · ${article.reviewTimeAgo()}")
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isSaved) {
                        stringResource(R.string.article_remove_save)
                    } else {
                        stringResource(R.string.article_save)
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onShowMenu) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.article_menu_title)
                )
            }
        }
    }
}

private fun NewsArticle.reviewTimeAgo(): CharSequence = DateUtils.getRelativeTimeSpanString(
    publicationDate.time,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS
)
