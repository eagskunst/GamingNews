package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleOpenModeUseCaseTest {

    private val fakeRepository = FakeUserPreferencesRepository()

    @Test
    fun `when getArticleOpenMode is invoked then it returns the current mode`() = runTest {
        fakeRepository.updateArticleOpenMode(ArticleOpenMode.READER_MODE)

        val useCase = GetArticleOpenModeUseCase(fakeRepository)

        assertEquals(ArticleOpenMode.READER_MODE, useCase().first())
    }

    @Test
    fun `when updateArticleOpenMode is invoked then the stored mode is updated`() = runTest {
        val useCase = UpdateArticleOpenModeUseCase(fakeRepository)

        useCase(ArticleOpenMode.EXTERNAL_BROWSER)

        assertEquals(ArticleOpenMode.EXTERNAL_BROWSER, fakeRepository.userPreferences.first().articleOpenMode)
    }
}
