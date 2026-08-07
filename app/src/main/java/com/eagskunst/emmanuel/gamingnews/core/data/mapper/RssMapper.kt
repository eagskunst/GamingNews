package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import android.text.Html
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.prof.rssparser.Article
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RSS_DATE_PATTERNS = listOf(
    "EEE, dd MMM yyyy HH:mm:ss z",
    "EEE, dd MMM yyyy HH:mm:ss Z",
    "dd MMM yyyy HH:mm:ss z",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
)

fun Article.toNewsArticle(sourceName: String): NewsArticle {
    val content = description ?: ""
    return NewsArticle(
        link = link ?: "",
        title = title ?: "",
        description = formatDescription(content),
        imageUrl = image,
        publicationDate = parseDate(pubDate),
        sourceName = sourceName
    )
}

private fun parseDate(dateString: String?): Date {
    if (dateString.isNullOrBlank()) return Date()
    for (pattern in RSS_DATE_PATTERNS) {
        try {
            return SimpleDateFormat(pattern, Locale.US).parse(dateString) ?: Date()
        } catch (_: ParseException) {
            // Try next pattern
        }
    }
    return Date()
}

private fun formatDescription(content: String): String {
    if (content.isBlank()) return ""
    val text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString()
        .replace(65532.toChar(), ' ')
        .trim()
    val builder = StringBuilder()
    val dotIndex = text.indexOf('.')
    if (dotIndex != -1) {
        builder.append(text.substring(0, dotIndex + 1))
    } else {
        builder.append(text)
    }
    if (builder.length > 180) {
        builder.delete(180, builder.length)
        builder.append("...")
    }
    return builder.toString()
}
