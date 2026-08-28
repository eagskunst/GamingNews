package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ArticleReaderRepository
import javax.inject.Inject

class GetReaderArticleUseCase @Inject constructor(
    private val repository: ArticleReaderRepository
) {
    suspend operator fun invoke(url: String): ReaderArticle? = repository.fetchArticle(url)
}
