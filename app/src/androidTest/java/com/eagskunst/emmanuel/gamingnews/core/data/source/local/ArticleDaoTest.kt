package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var articleDao: ArticleDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        articleDao = database.articleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `when articles are inserted and observed then they are returned ordered by savedAt descending`() = runTest {
        val older = articleEntity(
            link = "https://example.com/older",
            savedAt = Date(1_000L)
        )
        val newer = articleEntity(
            link = "https://example.com/newer",
            savedAt = Date(2_000L)
        )

        articleDao.insert(older)
        articleDao.insert(newer)

        val observed = articleDao.observeAll().first()

        assertEquals(2, observed.size)
        assertEquals(newer.link, observed[0].link)
        assertEquals(older.link, observed[1].link)
    }

    @Test
    fun `when an article is inserted with the same link then the existing row is replaced`() = runTest {
        val original = articleEntity(
            link = "https://example.com/article",
            title = "Original title",
            savedAt = Date(1_000L)
        )
        val updated = original.copy(
            title = "Updated title",
            savedAt = Date(2_000L)
        )

        articleDao.insert(original)
        articleDao.insert(updated)

        val stored = articleDao.getByLink(original.link)

        assertEquals("Updated title", stored?.title)
        assertEquals(Date(2_000L), stored?.savedAt)
    }

    @Test
    fun `when getByLink is called with an existing or missing link then it returns the correct entity or null`() = runTest {
        val article = articleEntity(link = "https://example.com/article")

        articleDao.insert(article)

        assertEquals(article.title, articleDao.getByLink(article.link)?.title)
        assertNull(articleDao.getByLink("https://example.com/missing"))
    }

    @Test
    fun `when an article is deleted then it is removed from observeAll`() = runTest {
        val article = articleEntity(link = "https://example.com/article")

        articleDao.insert(article)
        articleDao.delete(article)

        val observed = articleDao.observeAll().first()

        assertTrue(observed.isEmpty())
    }

    private fun articleEntity(
        link: String,
        title: String = "Title for $link",
        savedAt: Date = Date()
    ) = ArticleEntity(
        link = link,
        title = title,
        description = "Description for $link",
        imageUrl = null,
        publicationDate = Date(160_945_920_000L),
        sourceName = "Test Source",
        savedAt = savedAt
    )
}
