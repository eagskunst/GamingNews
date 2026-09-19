package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class IgdbRemoteDataSourceTest {

    private val testDispatchers = TestDispatcherProvider()

    private val api: IgdbApi = mockk()
    private val authLocalDataSource: IgdbAuthLocalDataSource = mockk(relaxed = true)
    private val authRemoteDataSource: IgdbAuthRemoteDataSource = mockk(relaxed = true)

    private val clientId = "test-client-id"

    private val dataSource = IgdbRemoteDataSource(
        api = api,
        authLocalDataSource = authLocalDataSource,
        authRemoteDataSource = authRemoteDataSource,
        clientId = clientId,
        dispatchers = testDispatchers
    )

    @Test
    fun `given cached access token when fetchUpcomingReleases then uses it and does not fetch a new one`() = runTest {
        val cachedToken = "cached-token"
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken(clientId) } returns cachedToken
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns emptyList()

        dataSource.fetchUpcomingReleases(0)

        coVerify(exactly = 0) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 1) { authLocalDataSource.getAccessToken(clientId) }

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $cachedToken", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 0)
    }

    @Test
    fun `given no cached access token when fetchUpcomingReleases then fetches saves and uses a new token`() = runTest {
        val freshToken = "fresh-token"
        val expiresIn = 3600L
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken(clientId) } returns null
        coEvery { authRemoteDataSource.fetchAccessToken() } returns (freshToken to expiresIn)
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns emptyList()

        dataSource.fetchUpcomingReleases(0)

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 1) { authLocalDataSource.saveAccessToken(freshToken, expiresIn, clientId) }

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $freshToken", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 0)
    }

    @Test
    fun `when fetchUpcomingReleases then passes Client-ID and Authorization headers to api`() = runTest {
        val token = "token"
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken(clientId) } returns token
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns listOf(mockk<IgdbReleaseDateDto>(relaxed = true))

        dataSource.fetchUpcomingReleases(42)

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $token", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 42)
    }

    @Test
    fun `GIVEN cached token receives 401 WHEN releases are fetched THEN replacement token retries once`() = runTest {
        coEvery { authLocalDataSource.getAccessToken(clientId) } returns "cached-token"
        coEvery { authRemoteDataSource.fetchAccessToken() } returns ("replacement-token" to 3_600L)
        coEvery { api.getReleaseDates(clientId, "Bearer cached-token", any()) } throws httpException(401)
        coEvery { api.getReleaseDates(clientId, "Bearer replacement-token", any()) } returns emptyList()

        dataSource.fetchUpcomingReleases(12)

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerifyOrder {
            api.getReleaseDates(clientId, "Bearer cached-token", any())
            authLocalDataSource.invalidateAccessToken("cached-token", clientId)
            authRemoteDataSource.fetchAccessToken()
            authLocalDataSource.saveAccessToken("replacement-token", 3_600L, clientId)
            api.getReleaseDates(clientId, "Bearer replacement-token", any())
        }
    }

    @Test
    fun `GIVEN replacement token also receives 401 WHEN releases are fetched THEN no further renewal occurs`() = runTest {
        coEvery { authLocalDataSource.getAccessToken(clientId) } returns "cached-token"
        coEvery { authRemoteDataSource.fetchAccessToken() } returns ("replacement-token" to 3_600L)
        coEvery { api.getReleaseDates(clientId, "Bearer cached-token", any()) } throws httpException(401)
        coEvery { api.getReleaseDates(clientId, "Bearer replacement-token", any()) } throws httpException(401)

        assertSuspendFails<IgdbRejectedTokenException> { dataSource.fetchUpcomingReleases() }

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 2) { api.getReleaseDates(clientId, any(), any()) }
    }

    @Test
    fun `GIVEN token acquisition fails WHEN releases are fetched THEN failure propagates without looping`() = runTest {
        coEvery { authLocalDataSource.getAccessToken(clientId) } returns null
        coEvery { authRemoteDataSource.fetchAccessToken() } throws IllegalStateException("unavailable")

        assertSuspendFails<IgdbTokenAcquisitionException> { dataSource.fetchUpcomingReleases() }

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 0) { api.getReleaseDates(any(), any(), any()) }
    }

    @Test
    fun `GIVEN non-401 response WHEN releases are fetched THEN token is not renewed`() = runTest {
        coEvery { authLocalDataSource.getAccessToken(clientId) } returns "cached-token"
        coEvery { api.getReleaseDates(clientId, "Bearer cached-token", any()) } throws httpException(500)

        assertSuspendFails<HttpException> { dataSource.fetchUpcomingReleases() }

        coVerify(exactly = 0) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 1) { api.getReleaseDates(clientId, "Bearer cached-token", any()) }
    }

    @Test
    fun `GIVEN retry after 401 WHEN releases are fetched THEN query and pagination offset are preserved`() = runTest {
        val bodies = mutableListOf<RequestBody>()
        coEvery { authLocalDataSource.getAccessToken(clientId) } returns "cached-token"
        coEvery { authRemoteDataSource.fetchAccessToken() } returns ("replacement-token" to 3_600L)
        coEvery { api.getReleaseDates(clientId, any(), capture(bodies)) } answers {
            if (secondArg<String>() == "Bearer cached-token") throw httpException(401)
            emptyList()
        }

        dataSource.fetchUpcomingReleases(73)

        assertEquals(2, bodies.size)
        assertEquals(bodies[0].bodyString(), bodies[1].bodyString())
        assertRequestBodyContainsQueryParts(bodies[1], offset = 73)
    }

    @Test
    fun `GIVEN concurrent 401 responses WHEN releases are fetched THEN renewal is shared`() = runTest {
        var storedToken: String? = "cached-token"
        var rejectedRequests = 0
        val bothRejected = CompletableDeferred<Unit>()
        coEvery { authLocalDataSource.getAccessToken(clientId) } answers { storedToken }
        coEvery { authLocalDataSource.invalidateAccessToken("cached-token", clientId) } answers {
            if (storedToken == "cached-token") storedToken = null
        }
        coEvery { authRemoteDataSource.fetchAccessToken() } returns ("replacement-token" to 3_600L)
        coEvery { authLocalDataSource.saveAccessToken("replacement-token", 3_600L, clientId) } answers {
            storedToken = "replacement-token"
        }
        coEvery { api.getReleaseDates(clientId, "Bearer cached-token", any()) } coAnswers {
            rejectedRequests++
            if (rejectedRequests == 2) bothRejected.complete(Unit)
            bothRejected.await()
            throw httpException(401)
        }
        coEvery { api.getReleaseDates(clientId, "Bearer replacement-token", any()) } returns emptyList()

        awaitAll(
            async { dataSource.fetchUpcomingReleases(0) },
            async { dataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT) }
        )

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 2) { api.getReleaseDates(clientId, "Bearer replacement-token", any()) }
    }

    private fun httpException(code: Int): HttpException = HttpException(
        Response.error<List<IgdbReleaseDateDto>>(code, "error".toResponseBody())
    )

    private suspend inline fun <reified T : Throwable> assertSuspendFails(block: suspend () -> Unit): T {
        try {
            block()
        } catch (exception: Throwable) {
            assertTrue("Expected ${T::class.java.name}, got ${exception::class.java.name}", exception is T)
            return exception as T
        }
        throw AssertionError("Expected ${T::class.java.name}")
    }

    private fun assertRequestBodyContainsQueryParts(body: RequestBody, offset: Int) {
        val text = body.bodyString()
        assertTrue(text.isNotBlank())
        assertTrue(text.contains("fields id,date,human,platform,game.name,game.url,game.cover.url"))
        assertTrue(text.contains("limit ${IgdbRemoteDataSource.PAGE_LIMIT}"))
        assertTrue(text.contains("offset $offset"))
        assertTrue(text.contains("platform = (6,49,48,130,167,169,508)"))
    }

    private fun RequestBody.bodyString(): String {
        val buffer = okio.Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
    }
}
