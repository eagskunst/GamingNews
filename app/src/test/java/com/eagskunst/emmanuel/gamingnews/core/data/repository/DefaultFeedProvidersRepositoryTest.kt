package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FeedProvidersLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultFeedProvidersRepositoryTest {

    private lateinit var localDataSource: FeedProvidersLocalDataSource
    private lateinit var repository: DefaultFeedProvidersRepository

    @Before
    fun setUp() = runTest {
        val context = RuntimeEnvironment.getApplication()
        localDataSource = FeedProvidersLocalDataSource(context)
        localDataSource.restoreDefaults()
        repository = DefaultFeedProvidersRepository(context, localDataSource)
    }

    @Test
    fun `given all providers enabled when providersStream for ALL then all are enabled`() = runTest {
        val providers = repository.providersStream(NewsCategory.ALL).first()

        assertTrue(providers.isNotEmpty())
        assertTrue(providers.all { it.enabled })
        assertTrue(providers.all { it.category == NewsCategory.ALL })
    }

    @Test
    fun `given a provider disabled when providersStream then that provider is disabled`() = runTest {
        val allProviders = repository.providersStream(NewsCategory.ALL).first()
        val firstId = allProviders.first().id

        localDataSource.setProviderEnabled(firstId, false)

        val updated = repository.providersStream(NewsCategory.ALL).first()
        val disabledProvider = updated.first { it.id == firstId }
        assertFalse(disabledProvider.enabled)
    }

    @Test
    fun `given no category when providersStream then returns all providers across categories`() = runTest {
        val allProviders = repository.providersStream(null).first()
        val categories = allProviders.map { it.category }.toSet()

        assertTrue(categories.size > 1)
    }

    @Test
    fun `given SONY category when providersStream then returns only sony providers`() = runTest {
        val providers = repository.providersStream(NewsCategory.SONY).first()

        assertTrue(providers.isNotEmpty())
        assertTrue(providers.all { it.category == NewsCategory.SONY })
    }

    @Test
    fun `given disabled provider when restoreDefaults then all providers are enabled`() = runTest {
        val allProviders = repository.providersStream(NewsCategory.ALL).first()
        val firstId = allProviders.first().id
        localDataSource.setProviderEnabled(firstId, false)

        repository.restoreDefaults()

        val updated = repository.providersStream(NewsCategory.ALL).first()
        assertTrue(updated.all { it.enabled })
    }

    @Test
    fun `given provider when setProviderEnabled false then provider is disabled`() = runTest {
        val allProviders = repository.providersStream(NewsCategory.ALL).first()
        val firstId = allProviders.first().id

        repository.setProviderEnabled(firstId, false)

        val updated = repository.providersStream(NewsCategory.ALL).first()
        assertFalse(updated.first { it.id == firstId }.enabled)
    }

    @Test
    fun `given each category when providersStream then every provider has a non-empty name and url`() = runTest {
        val allProviders = repository.providersStream(null).first()

        allProviders.forEach { provider ->
            assertTrue("Provider id is blank: $provider", provider.id.isNotBlank())
            assertTrue("Provider name is blank: $provider", provider.name.isNotBlank())
            assertTrue("Provider url is blank: $provider", provider.url.isNotBlank())
        }
    }

    @Test
    fun `given all providers when checking IDs then no duplicates exist`() = runTest {
        val allProviders = repository.providersStream(null).first()
        val ids = allProviders.map { it.id }

        assertEquals("Duplicate provider IDs found", ids.size, ids.toSet().size)
    }
}
