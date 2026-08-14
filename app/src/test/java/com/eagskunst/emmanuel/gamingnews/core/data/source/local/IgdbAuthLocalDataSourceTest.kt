package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class IgdbAuthLocalDataSourceTest {

    private val dataSource = IgdbAuthLocalDataSource(RuntimeEnvironment.getApplication())

    @Before
    fun clearStoredToken() = runTest {
        dataSource.clear()
    }

    @Test
    fun `given nothing stored when getAccessToken then returns null`() = runTest {
        assertNull(dataSource.getAccessToken())
    }

    @Test
    fun `given saved token not expired when getAccessToken then returns the token`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 3_600)

        assertEquals("some-token", dataSource.getAccessToken())
    }

    @Test
    fun `given saved token already expired when getAccessToken then returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = -10)

        assertNull(dataSource.getAccessToken())
    }

    @Test
    fun `given saved token when clear then getAccessToken returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 3_600)

        dataSource.clear()

        assertNull(dataSource.getAccessToken())
    }
}
