package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReviewArticle
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReviewCatalogDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.RssRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FailedReviewSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewEligibilityPolicy
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewEligibilityResult
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewSource
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReviewsRepository
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.supervisorScope
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultReviewsRepository @Inject constructor(
    private val rssRemoteDataSource: RssRemoteDataSource,
    private val catalogDataSource: ReviewCatalogDataSource,
    private val dispatchers: DispatcherProvider
) : ReviewsRepository {

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun reviewsStream(forceRefresh: Boolean): Flow<Result<ReviewFeedSnapshot>> = flow {
        val source = catalogDataSource.selectedSource(Locale.getDefault())
        if (source == null) {
            emit(Result.Success(ReviewFeedSnapshot(emptyList())))
            return@flow
        }

        val cached = cache[source.id]
        if (!forceRefresh && cached != null && !cached.isStale) {
            emit(Result.Success(cached.snapshot))
            return@flow
        }

        emit(Result.Loading)

        try {
            val snapshot = fetchSnapshot(listOf(source))
            when {
                snapshot.articles.isEmpty() && snapshot.failedSources.isNotEmpty() && cached == null -> {
                    emit(Result.Error(snapshot.failedSources.first().toException()))
                }
                cached != null && snapshot.articles.isEmpty() && snapshot.failedSources.isNotEmpty() -> {
                    emit(Result.Success(cached.snapshot.copy(failedSources = snapshot.failedSources)))
                }
                else -> {
                    cache[source.id] = CacheEntry(snapshot)
                    emit(Result.Success(snapshot))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cached != null) {
                emit(Result.Success(cached.snapshot.withFailedSource(source, e)))
            } else {
                emit(Result.Error(e))
            }
        }
    }.flowOn(dispatchers.io)

    private suspend fun fetchSnapshot(sources: List<ReviewSource>): ReviewFeedSnapshot = supervisorScope {
        val results = sources.map { source ->
            async {
                try {
                    when (source.eligibilityPolicy) {
                        ReviewEligibilityPolicy.GamingFeedReviews -> fetchEligibleReviews(source)
                        ReviewEligibilityPolicy.UnsupportedMixedContent -> SourceResult.UnsupportedMetadata(source)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    SourceResult.Error(source, e)
                }
            }
        }.awaitAll()

        val articles = results.filterIsInstance<SourceResult.Success>().flatMap { it.articles }
        val failed = results.filterIsInstance<SourceResult.Failure>().map { it.toFailedReviewSource() }

        ReviewFeedSnapshot(
            articles = articles.distinctByArticleIdentity(),
            failedSources = failed
        )
    }

    private suspend fun fetchEligibleReviews(source: ReviewSource): SourceResult {
        val channel = rssRemoteDataSource.fetchChannel(source.url)
        val articles = channel.items
            .mapNotNull { item ->
                val eligibility = classifyItem(item, source.eligibilityPolicy)
                when (eligibility) {
                    ReviewEligibilityResult.GAME -> item.toReviewArticle(source.name)
                    ReviewEligibilityResult.NON_GAME,
                    ReviewEligibilityResult.UNKNOWN -> null
                }
            }
            .filter { it.title.isNotBlank() && isHttpOrHttps(it.link) }
        return SourceResult.Success(source, articles)
    }

    private fun classifyItem(item: RssItem, policy: ReviewEligibilityPolicy): ReviewEligibilityResult {
        return when (policy) {
            ReviewEligibilityPolicy.GamingFeedReviews -> ReviewEligibilityResult.GAME
            ReviewEligibilityPolicy.UnsupportedMixedContent -> ReviewEligibilityResult.UNKNOWN
        }
    }

    private fun List<NewsArticle>.distinctByArticleIdentity(): List<NewsArticle> {
        val seenUrls = mutableSetOf<String>()
        return sortedByDescending { it.publicationDate }
            .filter { article ->
                val canonicalUrl = article.link.stripFragment()
                seenUrls.add(canonicalUrl)
            }
    }

    private fun String.stripFragment(): String {
        val fragmentIndex = indexOf('#')
        return if (fragmentIndex == -1) this else substring(0, fragmentIndex)
    }

    private fun isHttpOrHttps(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)
    }

    private fun ReviewFeedSnapshot.withFailedSource(source: ReviewSource, exception: Exception): ReviewFeedSnapshot {
        val failure = FailedReviewSource(
            id = source.id,
            name = source.name,
            reason = FailedReviewSource.FailureReason.NetworkError(exception)
        )
        return copy(failedSources = failedSources + failure)
    }

    private sealed interface SourceResult {
        data class Success(val source: ReviewSource, val articles: List<NewsArticle>) : SourceResult
        sealed interface Failure : SourceResult {
            val source: ReviewSource
            fun toFailedReviewSource(): FailedReviewSource
        }

        data class Error(override val source: ReviewSource, val exception: Exception) : Failure {
            override fun toFailedReviewSource(): FailedReviewSource = FailedReviewSource(
                id = source.id,
                name = source.name,
                reason = FailedReviewSource.FailureReason.NetworkError(exception)
            )
        }

        data class UnsupportedMetadata(override val source: ReviewSource) : Failure {
            override fun toFailedReviewSource(): FailedReviewSource = FailedReviewSource(
                id = source.id,
                name = source.name,
                reason = FailedReviewSource.FailureReason.UnsupportedEligibilityMetadata
            )
        }
    }

    private data class CacheEntry(
        val snapshot: ReviewFeedSnapshot,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val isStale: Boolean
            get() = System.currentTimeMillis() - timestamp > CACHE_FRESHNESS_MS
    }

    private companion object {
        const val CACHE_FRESHNESS_MS = 15 * 60 * 1000L
    }
}
