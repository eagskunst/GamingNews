package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import android.content.Context
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FeedUrlsCategoryDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject

class GetFeedUrlsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val categories by lazy { loadCategories() }

    operator fun invoke(category: NewsCategory): List<String> {
        val locale = Locale.getDefault().language
        val matching = categories.find { it.language == locale }
            ?: categories.find { it.language == "en" }
            ?: return emptyList()

        return when (category) {
            NewsCategory.ALL -> matching.allUrls
            NewsCategory.PS4 -> matching.ps4Urls
            NewsCategory.XBOX -> matching.xboxUrls
            NewsCategory.SWITCH -> matching.switchUrls
            NewsCategory.PC -> matching.pcUrls
        }
    }

    private fun loadCategories(): List<FeedUrlsCategoryDto> {
        return context.assets.open("Urls.json").use { input ->
            json.decodeFromString<List<FeedUrlsCategoryDto>>(input.bufferedReader().use { it.readText() })
        }
    }
}
