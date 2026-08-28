package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle

interface ArticleReaderRepository {
    suspend fun fetchArticle(url: String): ReaderArticle?
}
