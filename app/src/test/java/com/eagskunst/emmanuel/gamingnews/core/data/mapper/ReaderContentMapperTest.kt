package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderContentMapperTest {

    private val baseUrl = "https://example.com/article"

    @Test
    fun `given cleaned article html when mapped then produces typed elements`() {
        val html = """
            <div>
                <h1>Title</h1>
                <p>First <strong>paragraph</strong>.</p>
                <img src="image.png" alt="Alt text">
                <figure>
                    <img src="figure.png">
                    <figcaption>Figure caption</figcaption>
                </figure>
                <ul><li>Item one</li><li>Item two</li></ul>
                <ol><li>First</li><li>Second</li></ol>
                <blockquote>A quote</blockquote>
                <hr>
                <div><p>Nested paragraph</p></div>
            </div>
        """.trimIndent()

        val elements = Jsoup.parseBodyFragment(html, baseUrl).body().toReaderElements(baseUrl)

        assertEquals(9, elements.size)
        assertTrue(elements[0] is ReaderElement.Heading)
        assertEquals("Title", (elements[0] as ReaderElement.Heading).text)
        assertTrue(elements[1] is ReaderElement.Paragraph)
        assertTrue(elements[2] is ReaderElement.Image)
        assertEquals("https://example.com/image.png", (elements[2] as ReaderElement.Image).url)
        assertEquals("Alt text", (elements[2] as ReaderElement.Image).caption)
        assertTrue(elements[3] is ReaderElement.Image)
        assertEquals("https://example.com/figure.png", (elements[3] as ReaderElement.Image).url)
        assertEquals("Figure caption", (elements[3] as ReaderElement.Image).caption)
        assertTrue(elements[4] is ReaderElement.BulletList)
        assertEquals(listOf("Item one", "Item two"), (elements[4] as ReaderElement.BulletList).items)
        assertTrue(elements[5] is ReaderElement.OrderedList)
        assertEquals(listOf("First", "Second"), (elements[5] as ReaderElement.OrderedList).items)
        assertTrue(elements[6] is ReaderElement.Quote)
        assertEquals("A quote", (elements[6] as ReaderElement.Quote).text)
        assertTrue(elements[7] is ReaderElement.Divider)
        assertTrue(elements[8] is ReaderElement.Paragraph)
        assertEquals("Nested paragraph", (elements[8] as ReaderElement.Paragraph).html)
    }

    @Test
    fun `given empty content when mapped then returns empty list`() {
        val html = "<div></div>"

        val elements = Jsoup.parseBodyFragment(html, baseUrl).body().toReaderElements(baseUrl)

        assertTrue(elements.isEmpty())
    }

    @Test
    fun `given paragraph with only whitespace when mapped then it is filtered out`() {
        val html = """
            <div>
                <p>   </p>
                <p>Real text</p>
            </div>
        """.trimIndent()

        val elements = Jsoup.parseBodyFragment(html, baseUrl).body().toReaderElements(baseUrl)

        assertEquals(1, elements.size)
        assertTrue(elements[0] is ReaderElement.Paragraph)
        assertEquals("Real text", (elements[0] as ReaderElement.Paragraph).html)
    }

    @Test
    fun `given absolute image url when mapped then it is unchanged`() {
        val html = """<img src="https://cdn.example.com/img.jpg">"""

        val elements = Jsoup.parseBodyFragment(html, baseUrl).body().toReaderElements(baseUrl)

        assertEquals(1, elements.size)
        val image = elements[0] as ReaderElement.Image
        assertEquals("https://cdn.example.com/img.jpg", image.url)
    }
}
