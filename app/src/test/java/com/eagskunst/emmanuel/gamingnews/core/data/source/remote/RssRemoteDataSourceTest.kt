package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RssRemoteDataSourceTest {

    @Test
    fun `given malformed url when fetchChannel then throws an exception`() = runTest {
        val dataSource = RssRemoteDataSource(TestDispatcherProvider())

        try {
            dataSource.fetchChannel("not a url")
            fail("Expected an exception to be thrown for an invalid URL")
        } catch (e: Exception) {
            assertTrue(true)
        }
    }
}
