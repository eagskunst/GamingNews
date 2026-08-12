package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository

class UpdateArticleOpenModeUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(mode: ArticleOpenMode) = repository.updateArticleOpenMode(mode)
}
