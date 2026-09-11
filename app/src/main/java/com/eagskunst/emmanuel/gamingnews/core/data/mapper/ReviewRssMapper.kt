package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import android.text.Html
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.prof18.rssparser.model.RssItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val REVIEW_DATE_PATTERNS = listOf(
    "EEE, dd MMM yyyy HH:mm:ss z",
    "EEE, dd MMM yyyy HH:mm:ss Z",
    "dd MMM yyyy HH:mm:ss z",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
)

internal val UNKNOWN_REVIEW_DATE: Date = Date(0)

fun RssItem.toReviewArticle(sourceName: String): NewsArticle? {
    val rawLink = link?.trim() ?: return null
    if (rawLink.isEmpty()) return null
    if (!isHttpOrHttps(rawLink)) return null

    val title = title?.trim().orEmpty()
    if (title.isEmpty()) return null

    val content = description?.trim().orEmpty()
    return NewsArticle(
        link = rawLink,
        title = title,
        description = formatReviewDescription(content),
        imageUrl = image?.trim()?.takeIf { it.isNotEmpty() },
        publicationDate = parseReviewDate(pubDate),
        sourceName = sourceName,
        author = author?.trim()?.takeIf { it.isNotEmpty() }
    )
}

private fun isHttpOrHttps(url: String): Boolean =
    url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)

internal fun parseReviewDate(dateString: String?): Date {
    if (dateString.isNullOrBlank()) return UNKNOWN_REVIEW_DATE
    return REVIEW_DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
        runCatching { SimpleDateFormat(pattern, Locale.US).parse(dateString) }.getOrNull()
    } ?: UNKNOWN_REVIEW_DATE
}

internal fun formatReviewDescription(content: String): String {
    if (content.isBlank()) return ""
    val text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString()
        .replace(65532.toChar(), ' ')
        .replace("\u00a0", " ")
        .lines()
        .joinToString(" ") { it.trim() }
        .trim()
    if (text.isBlank()) return ""
    return if (text.length > 280) {
        text.substring(0, 277).trimEnd() + "..."
    } else {
        text
    }
}
