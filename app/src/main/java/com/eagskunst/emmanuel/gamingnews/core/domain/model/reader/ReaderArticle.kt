package com.eagskunst.emmanuel.gamingnews.core.domain.model.reader

data class ReaderArticle(
    val title: String,
    val byline: String?,
    val siteName: String?,
    val elements: List<ReaderElement>
)

sealed interface ReaderElement {
    data class Paragraph(val html: String) : ReaderElement
    data class Heading(val level: Int, val text: String) : ReaderElement
    data class Image(val url: String, val caption: String?) : ReaderElement
    data class BulletList(val items: List<String>) : ReaderElement
    data class OrderedList(val items: List<String>) : ReaderElement
    data class Quote(val text: String) : ReaderElement
    data object Divider : ReaderElement
}
