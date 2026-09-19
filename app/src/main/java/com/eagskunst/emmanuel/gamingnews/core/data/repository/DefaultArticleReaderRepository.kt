package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReaderElements
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.ArticleReaderRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ArticleReaderRepository
import net.dankito.readability4j.extended.Readability4JExtended
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

private const val MIN_CONTENT_LENGTH = 100

class DefaultArticleReaderRepository @Inject constructor(
    private val remoteDataSource: ArticleReaderRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : ArticleReaderRepository {

    override suspend fun fetchArticle(url: String): ReaderArticle? = withContext(dispatchers.io) {
        val html = remoteDataSource.fetchHtml(url) ?: return@withContext null

        val article = Readability4JExtended(url, html).parse()
        val contentHtml = article.content
        if (contentHtml.isNullOrBlank() || (article.textContent?.length ?: 0) < MIN_CONTENT_LENGTH) {
            return@withContext null
        }

        val body = Jsoup.parseBodyFragment(contentHtml, url).body()
        val elements = body.toReaderElements(url)

        ReaderArticle(
            title = article.title ?: "",
            byline = article.byline,
            siteName = null,
            elements = elements
        )
    }
}
