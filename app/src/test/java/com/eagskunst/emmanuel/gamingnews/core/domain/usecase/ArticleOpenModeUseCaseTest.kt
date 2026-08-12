package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleOpenModeUseCaseTest {

    private val fakeRepository = FakeUserPreferencesRepository()

    @Test
    fun `getArticleOpenMode returns current mode`() = runTest {
        fakeRepository.updateArticleOpenMode(ArticleOpenMode.READER_MODE)

        val useCase = GetArticleOpenModeUseCase(fakeRepository)

        assertEquals(ArticleOpenMode.READER_MODE, useCase().first())
    }

    @Test
    fun `updateArticleOpenMode changes stored mode`() = runTest {
        val useCase = UpdateArticleOpenModeUseCase(fakeRepository)

        useCase(ArticleOpenMode.EXTERNAL_BROWSER)

        assertEquals(ArticleOpenMode.EXTERNAL_BROWSER, fakeRepository.userPreferences.first().articleOpenMode)
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(
            UserPreferences(
                darkTheme = false,
                loadImages = true,
                dailyReminder = false,
                articleOpenMode = ArticleOpenMode.CUSTOM_TAB
            )
        )

        override val userPreferences = preferencesFlow

        override suspend fun updateDarkTheme(enabled: Boolean) {
            preferencesFlow.value = preferencesFlow.value.copy(darkTheme = enabled)
        }

        override suspend fun updateLoadImages(enabled: Boolean) {
            preferencesFlow.value = preferencesFlow.value.copy(loadImages = enabled)
        }

        override suspend fun updateDailyReminder(enabled: Boolean) {
            preferencesFlow.value = preferencesFlow.value.copy(dailyReminder = enabled)
        }

        override suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
            preferencesFlow.value = preferencesFlow.value.copy(articleOpenMode = mode)
        }
    }
}
