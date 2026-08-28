package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private val BLOCK_LEVEL_TAGS = setOf(
    "address", "article", "aside", "blockquote", "canvas", "dd", "div", "dl", "dt",
    "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4",
    "h5", "h6", "header", "hr", "li", "main", "nav", "noscript", "ol", "p", "pre",
    "section", "table", "tfoot", "ul", "video"
)

private const val MIN_TEXT_LENGTH = 2

fun Element.toReaderElements(baseUrl: String): List<ReaderElement> {
    return children().flatMap { it.toReaderElement(baseUrl) }
}

private fun Element.toReaderElement(baseUrl: String): List<ReaderElement> {
    return when (tagName()) {
        "h1", "h2", "h3", "h4", "h5", "h6" -> {
            if (textNotBlank()) listOf(ReaderElement.Heading(level = tagName().drop(1).toInt(), text = text().trim())) else emptyList()
        }
        "p" -> asParagraph(baseUrl)
        "img" -> listOfNotNull(toImageElement(baseUrl))
        "figure" -> extractFigure(baseUrl)
        "ul" -> extractList(ordered = false, baseUrl)
        "ol" -> extractList(ordered = true, baseUrl)
        "blockquote" -> {
            if (textNotBlank()) listOf(ReaderElement.Quote(text = text().trim())) else emptyList()
        }
        "hr" -> listOf(ReaderElement.Divider)
        "div" -> {
            when {
                hasBlockChildren() -> children().flatMap { it.toReaderElement(baseUrl) }
                textNotBlank() -> asParagraph(baseUrl)
                else -> emptyList()
            }
        }
        else -> asParagraph(baseUrl)
    }
}

private fun Element.extractFigure(baseUrl: String): List<ReaderElement> {
    val image = selectFirst("img")?.toImageElement(baseUrl)
    val caption = selectFirst("figcaption")?.text()?.trim()?.takeIf { it.isNotBlank() }

    return when {
        image != null -> listOf(
            image.copy(
                caption = image.caption ?: caption,
                url = image.url
            )
        )
        caption != null -> listOf(ReaderElement.Paragraph(html = selectFirst("figcaption")?.html() ?: caption))
        else -> emptyList()
    }
}

private fun Element.extractList(ordered: Boolean, baseUrl: String): List<ReaderElement> {
    val items = select("li").map { it.readableText(baseUrl).trim() }.filter { it.isNotBlank() }
    return if (items.isNotEmpty()) {
        listOf(
            if (ordered) ReaderElement.OrderedList(items) else ReaderElement.BulletList(items)
        )
    } else {
        emptyList()
    }
}

private fun Element.asParagraph(baseUrl: String): List<ReaderElement> {
    val directImages = children().filter { it.tagName() == "img" }.mapNotNull { it.toImageElement(baseUrl) }
    if (directImages.isNotEmpty() && !textNotBlank()) {
        return directImages
    }

    val html = html().trim()
    val text = readableText(baseUrl).trim()
    return if (text.length >= MIN_TEXT_LENGTH) {
        listOf(ReaderElement.Paragraph(html = html))
    } else {
        emptyList()
    }
}

private fun Element.toImageElement(baseUrl: String): ReaderElement.Image? {
    val rawUrl = attr("src").takeIf { it.isNotBlank() }
        ?: attr("data-src").takeIf { it.isNotBlank() }
        ?: return null
    val resolvedUrl = resolveUrl(baseUrl, rawUrl)
    val caption = attr("alt").takeIf { it.isNotBlank() }
        ?: attr("title").takeIf { it.isNotBlank() }
    return ReaderElement.Image(url = resolvedUrl, caption = caption)
}

private fun Element.readableText(baseUrl: String): String = buildString {
    childNodes().forEach { append(it.readableText(baseUrl)) }
}

private fun Node.readableText(baseUrl: String): String = when (this) {
    is TextNode -> text()
    is Element -> {
        when (tagName()) {
            "br" -> "\n"
            "img" -> toImageElement(baseUrl)?.let { " " } ?: ""
            else -> readableText(baseUrl)
        }
    }
    else -> ""
}

private fun Element.hasBlockChildren(): Boolean = children().any { it.tagName() in BLOCK_LEVEL_TAGS }

private fun Element.textNotBlank(): Boolean = text().trim().isNotBlank()

private fun resolveUrl(baseUrl: String, url: String): String {
    if (url.startsWith("http://", ignoreCase = true) ||
        url.startsWith("https://", ignoreCase = true) ||
        url.startsWith("data:")
    ) {
        return url
    }
    return baseUrl.toHttpUrlOrNull()?.resolve(url)?.toString() ?: url
}
