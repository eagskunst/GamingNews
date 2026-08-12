package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetArticleOpenModeUseCase(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<ArticleOpenMode> =
        repository.userPreferences.map { it.articleOpenMode }
}
