package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewEligibilityPolicy
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewCatalogDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val catalog by lazy { loadCatalog() }

    fun selectedSource(locale: Locale = Locale.getDefault()): ReviewSource? {
        val language = locale.language
        return catalog.find { it.language == language }
            ?: catalog.find { it.language == "en" }
    }

    fun allSources(): List<ReviewSource> = catalog

    private fun loadCatalog(): List<ReviewSource> {
        return context.assets.open("review_feeds.json").use { input ->
            json.decodeFromString<List<ReviewFeedDto>>(input.bufferedReader().use { it.readText() })
        }.map { dto ->
            ReviewSource(
                id = dto.id,
                language = dto.language,
                name = dto.name,
                url = dto.url,
                eligibilityPolicy = policyFromName(dto.eligibilityPolicyName)
            )
        }
    }

    private fun policyFromName(name: String): ReviewEligibilityPolicy = when (name) {
        "gaming_feed_reviews" -> ReviewEligibilityPolicy.GamingFeedReviews
        "unsupported_mixed_content" -> ReviewEligibilityPolicy.UnsupportedMixedContent
        else -> ReviewEligibilityPolicy.UnsupportedMixedContent
    }
}
