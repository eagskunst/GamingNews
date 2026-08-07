package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository

class ToggleSavedArticleUseCase(private val repository: NewsRepository) {
    suspend operator fun invoke(article: NewsArticle) {
        if (repository.isArticleSaved(article)) {
            repository.removeArticle(article)
        } else {
            repository.saveArticle(article)
        }
    }
}
