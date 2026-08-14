package com.eagskunst.emmanuel.gamingnews.testutil

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * [DispatcherProvider] fake that routes every dispatcher to the same [TestDispatcher],
 * so coroutine-based code under test runs deterministically on the test thread.
 */
@ExperimentalCoroutinesApi
class TestDispatcherProvider(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : DispatcherProvider {
    override val io = dispatcher
    override val default = dispatcher
    override val main = dispatcher
}
