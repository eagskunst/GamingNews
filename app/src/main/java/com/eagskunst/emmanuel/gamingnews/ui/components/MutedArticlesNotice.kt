package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.eagskunst.emmanuel.gamingnews.R

/**
 * Tracks feed scroll direction so [MutedArticlesNotice] collapses while scrolling down and
 * reappears when scrolling up. Attach [nestedScrollConnection] to the scrollable content's
 * parent and gate the notice on [visible]. The notice shows again whenever [resetKey]
 * changes (e.g. the muted count).
 */
class MutedNoticeScrollBehavior internal constructor() {

    var visible by mutableStateOf(true)
        private set

    fun show() {
        visible = true
    }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            when {
                available.y < -SCROLL_EPSILON -> visible = false
                available.y > SCROLL_EPSILON -> visible = true
            }
            return Offset.Zero
        }
    }

    private companion object {
        const val SCROLL_EPSILON = 1f
    }
}

@Composable
fun rememberMutedNoticeScrollBehavior(resetKey: Any? = null): MutedNoticeScrollBehavior {
    val behavior = remember { MutedNoticeScrollBehavior() }
    LaunchedEffect(resetKey) { behavior.show() }
    return behavior
}

/**
 * Inline feed notice shown when mute rules hide articles in the current view.
 * Renders nothing when [mutedCount] is zero. Kept separate from refresh and stale-source banners.
 */
@Composable
fun MutedArticlesNotice(
    mutedCount: Int,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (mutedCount <= 0) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countText = if (revealed) {
                pluralStringResource(R.plurals.muted_articles_showing, mutedCount, mutedCount)
            } else {
                pluralStringResource(R.plurals.muted_articles_hidden, mutedCount, mutedCount)
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(countText) }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onToggleReveal) {
                Text(
                    text = stringResource(
                        if (revealed) R.string.muted_articles_hide_again
                        else R.string.muted_articles_show_hidden
                    )
                )
            }
            TextButton(onClick = onManage) {
                Text(text = stringResource(R.string.muted_articles_manage))
            }
        }
    }
}

/**
 * Empty state for a feed whose visible articles were all muted.
 */
@Composable
fun AllMutedEmptyState(
    onShowHidden: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.all_muted_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.all_muted_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShowHidden) {
                Text(text = stringResource(R.string.muted_articles_show_hidden))
            }
            TextButton(onClick = onManage) {
                Text(text = stringResource(R.string.muted_articles_manage))
            }
        }
    }
}

/**
 * Accessible badge marking a revealed muted article.
 */
@Composable
fun MutedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = stringResource(R.string.muted_articles_badge),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
