package com.eagskunst.emmanuel.gamingnews.ui.reader

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.utility.openCustomTab
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val READER_SCRIPT_NAME = "ReaderBridge"
private const val READER_CSS_LIGHT = """
    <style>
        :root { color-scheme: light; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            line-height: 1.6;
            color: #1f1f1f;
            background: #ffffff;
            max-width: 720px;
            margin: 0 auto;
            padding: 16px;
        }
        h1 { font-size: 1.6em; margin-bottom: 0.3em; }
        .byline { color: #666; font-size: 0.9em; margin-bottom: 1.5em; }
        img { max-width: 100%; height: auto; border-radius: 8px; }
        a { color: #0066cc; }
        figure { margin: 1em 0; }
    </style>
"""
private const val READER_CSS_DARK = """
    <style>
        :root { color-scheme: dark; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            line-height: 1.6;
            color: #e3e3e3;
            background: #121212;
            max-width: 720px;
            margin: 0 auto;
            padding: 16px;
        }
        h1 { font-size: 1.6em; margin-bottom: 0.3em; }
        .byline { color: #9aa0a6; font-size: 0.9em; margin-bottom: 1.5em; }
        img { max-width: 100%; height: auto; border-radius: 8px; }
        a { color: #8ab4f8; }
        figure { margin: 1em 0; }
    </style>
"""

@Serializable
private data class ReaderArticle(
    val title: String = "",
    val byline: String? = null,
    @SerialName("siteName") val siteName: String? = null,
    val content: String = ""
)

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
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    var isParsed by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxSize()) {
                        ReaderWebView(
                            articleUrl = state.url,
                            html = state.html,
                            isDarkTheme = isDarkTheme,
                            onParsed = { isParsed = true },
                            onParseFailed = {
                                context.openCustomTab(state.url.toUri())
                            }
                        )
                        if (!isParsed) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
private fun ReaderWebView(
    articleUrl: String,
    html: String,
    isDarkTheme: Boolean,
    onParsed: () -> Unit,
    onParseFailed: () -> Unit
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var hasParsed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }

    AndroidView(
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean = false

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        if (hasParsed) return
                        injectReaderScripts(view, articleUrl, isDarkTheme)
                    }
                }
                addJavascriptInterface(
                    ReaderBridge(
                        view = this,
                        onParsed = {
                            hasParsed = true
                            onParsed()
                        },
                        onParseFailed = onParseFailed
                    ),
                    READER_SCRIPT_NAME
                )
                loadDataWithBaseURL(articleUrl, html, "text/html", "UTF-8", null)
            }.also { webView = it }
        },
        update = { /* no-op; the same WebView is reused */ },
        modifier = Modifier.fillMaxSize()
    )
}

private fun injectReaderScripts(
    view: WebView,
    articleUrl: String,
    isDarkTheme: Boolean
) {
    try {
        val readabilityJs = view.context.assets.open("readability.js").bufferedReader().use { it.readText() }
        view.evaluateJavascript(readabilityJs, null)

        val parseScript = """
            (function() {
                try {
                    var article = new Readability(document, { uri: '${articleUrl.escapeJsString()}' }).parse();
                    if (article && article.content && article.content.length > 100) {
                        window.$READER_SCRIPT_NAME.onReaderContent(
                            JSON.stringify(article),
                            $isDarkTheme
                        );
                    } else {
                        window.$READER_SCRIPT_NAME.onReaderError('parse_failed');
                    }
                } catch (e) {
                    window.$READER_SCRIPT_NAME.onReaderError(e.message || 'unknown');
                }
            })();
        """.trimIndent()
        view.evaluateJavascript(parseScript, null)
    } catch (e: Exception) {
        Log.e("ReaderScreen", "Failed to inject reader scripts", e)
        view.context.openCustomTab(articleUrl.toUri())
    }
}

private fun buildReaderHtml(article: ReaderArticle, isDarkTheme: Boolean): String {
    val css = if (isDarkTheme) READER_CSS_DARK else READER_CSS_LIGHT
    val byline = article.byline?.takeIf { it.isNotBlank() }
        ?: article.siteName?.takeIf { it.isNotBlank() } ?: ""
    val bylineHtml = if (byline.isNotBlank()) "<div class='byline'>${byline.htmlEscape()}</div>" else ""

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            $css
        </head>
        <body>
            <h1>${article.title.htmlEscape()}</h1>
            $bylineHtml
            ${article.content}
        </body>
        </html>
    """.trimIndent()
}

private fun loadCleanHtml(view: WebView, html: String, isDarkTheme: Boolean) {
    view.loadDataWithBaseURL(view.url, html, "text/html", "UTF-8", null)
    view.setBackgroundColor(if (isDarkTheme) 0xff121212.toInt() else 0xffffffff.toInt())
}

private class ReaderBridge(
    private val view: WebView,
    private val onParsed: () -> Unit,
    private val onParseFailed: () -> Unit
) {
    @JavascriptInterface
    fun onReaderContent(json: String, isDarkTheme: Boolean) {
        ensureMainThread {
            try {
                val article = Json.decodeFromString(ReaderArticle.serializer(), json)
                val html = buildReaderHtml(article, isDarkTheme)
                loadCleanHtml(view, html, isDarkTheme)
                onParsed()
            } catch (e: Exception) {
                Log.e("ReaderBridge", "Failed to parse reader content", e)
                onParseFailed()
            }
        }
    }

    @JavascriptInterface
    fun onReaderError(error: String) {
        Log.e("ReaderBridge", "Readability error: $error")
        ensureMainThread { onParseFailed() }
    }

    private fun ensureMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            view.post { action() }
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

private fun String.htmlEscape(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

private fun String.escapeJsString(): String {
    return this
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}
