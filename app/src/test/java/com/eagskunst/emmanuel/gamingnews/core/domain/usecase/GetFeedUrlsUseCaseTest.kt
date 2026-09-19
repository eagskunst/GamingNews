package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFeedProvidersRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetFeedUrlsUseCaseTest {

    private lateinit var repository: FakeFeedProvidersRepository
    private lateinit var useCase: GetFeedUrlsUseCase

    @Before
    fun setUp() {
        repository = FakeFeedProvidersRepository()
        useCase = GetFeedUrlsUseCase(repository)
    }

    @Test
    fun `given all providers enabled when invoke then returns all URLs`() = runTest {
        val urls = useCase(NewsCategory.ALL)

        assertEquals(listOf("https://example.com/feed"), urls)
    }

    @Test
    fun `given a provider disabled when invoke then returns only enabled URLs`() = runTest {
        repository.providersFlow.value = listOf(
            FeedProvider("a", "A", "https://a.com/feed", NewsCategory.ALL, enabled = true),
            FeedProvider("b", "B", "https://b.com/feed", NewsCategory.ALL, enabled = false)
        )

        val urls = useCase(NewsCategory.ALL)

        assertEquals(listOf("https://a.com/feed"), urls)
    }

    @Test
    fun `given all providers disabled when invoke then returns empty list`() = runTest {
        repository.providersFlow.value = listOf(
            FeedProvider("a", "A", "https://a.com/feed", NewsCategory.ALL, enabled = false)
        )

        val urls = useCase(NewsCategory.ALL)

        assertEquals(emptyList<String>(), urls)
    }
}
