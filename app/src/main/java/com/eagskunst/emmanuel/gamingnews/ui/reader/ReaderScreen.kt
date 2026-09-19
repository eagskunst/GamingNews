package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import com.eagskunst.emmanuel.gamingnews.utility.openCustomTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    isDarkTheme: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadImages by viewModel.loadImages.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    BackHandler(enabled = true, onBack = onBackClick)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reader_mode_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            when (val state = uiState) {
                is ReaderUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReaderUiState.Error -> {
                    ReaderError(
                        onRetry = { viewModel.retry() },
                        onOpenInBrowser = { context.openCustomTab(viewModel.articleUrl.toUri()) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ReaderUiState.Content -> {
                    ReaderContent(
                        article = state.article,
                        loadImages = loadImages,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderContent(
    article: com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle,
    loadImages: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "title") {
            if (article.title.isNotBlank()) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "byline") {
            val byline = article.byline?.takeIf { it.isNotBlank() }
                ?: article.siteName?.takeIf { it.isNotBlank() }
            if (byline != null) {
                Text(
                    text = byline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        items(
            items = article.elements,
            key = { element -> element.hashCode() }
        ) { element ->
            ReaderElementItem(element = element, loadImages = loadImages)
        }
    }
}

@Composable
private fun ReaderElementItem(
    element: ReaderElement,
    loadImages: Boolean,
    modifier: Modifier = Modifier
) {
    when (element) {
        is ReaderElement.Heading -> {
            val style = when (element.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            }
            Text(
                text = element.text,
                style = style,
                modifier = modifier.fillMaxWidth()
            )
        }
        is ReaderElement.Paragraph -> {
            val styledText = rememberStyledParagraph(element.html)
            Text(
                text = styledText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.fillMaxWidth()
            )
        }
        is ReaderElement.Image -> {
            if (loadImages) {
                AsyncImage(
                    model = element.url,
                    contentDescription = element.caption
                        ?: stringResource(R.string.reader_image_content_description),
                    modifier = modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentScale = ContentScale.FillWidth
                )
                element.caption?.let { caption ->
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            } else {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(R.string.reader_image_hidden),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        is ReaderElement.BulletList -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                element.items.forEach { item ->
                    Text(
                        text = "\u2022\u00A0$item",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        is ReaderElement.OrderedList -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                element.items.forEachIndexed { index, item ->
                    Text(
                        text = "${index + 1}.\u00A0$item",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        is ReaderElement.Quote -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = element.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is ReaderElement.Divider -> {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun rememberStyledParagraph(html: String): AnnotatedString {
    return androidx.compose.runtime.remember(html) {
        try {
            AnnotatedString.fromHtml(html)
        } catch (_: Exception) {
            AnnotatedString(html)
        }
    }
}

@Composable
private fun ReaderError(
    onRetry: () -> Unit,
    onOpenInBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.reader_error_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.reader_retry_button))
        }
        Button(onClick = onOpenInBrowser) {
            Text(stringResource(R.string.reader_open_browser_button))
        }
    }
}
