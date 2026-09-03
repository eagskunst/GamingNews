package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFeedProvidersRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FeedProvidersUseCasesTest {

    private lateinit var repository: FakeFeedProvidersRepository
    private lateinit var getProvidersUseCase: GetFeedProvidersUseCase
    private lateinit var setEnabledUseCase: SetFeedProviderEnabledUseCase
    private lateinit var restoreDefaultsUseCase: RestoreFeedProvidersDefaultsUseCase

    @Before
    fun setUp() {
        repository = FakeFeedProvidersRepository()
        getProvidersUseCase = GetFeedProvidersUseCase(repository)
        setEnabledUseCase = SetFeedProviderEnabledUseCase(repository)
        restoreDefaultsUseCase = RestoreFeedProvidersDefaultsUseCase(repository)
    }

    @Test
    fun `given all enabled when getProviders then returns all providers enabled`() = runTest {
        val providers = getProvidersUseCase(NewsCategory.ALL).first()

        assertTrue(providers.isNotEmpty())
        assertTrue(providers.all { it.enabled })
    }

    @Test
    fun `given a disabled provider when setEnabled false then provider is disabled`() = runTest {
        val providerId = getProvidersUseCase(NewsCategory.ALL).first().first().id

        setEnabledUseCase(providerId, false)

        val providers = getProvidersUseCase(NewsCategory.ALL).first()
        assertFalse(providers.first { it.id == providerId }.enabled)
    }

    @Test
    fun `given disabled providers when restoreDefaults then all are re-enabled`() = runTest {
        val providerId = getProvidersUseCase(NewsCategory.ALL).first().first().id
        setEnabledUseCase(providerId, false)

        restoreDefaultsUseCase()

        val providers = getProvidersUseCase(NewsCategory.ALL).first()
        assertTrue(providers.all { it.enabled })
    }
}
