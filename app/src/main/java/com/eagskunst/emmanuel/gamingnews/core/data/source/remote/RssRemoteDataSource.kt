package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.prof.rssparser.Channel
import com.prof.rssparser.OnTaskCompleted
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RssRemoteDataSource(
    private val parser: Parser,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun fetchChannel(url: String): Channel = withContext(dispatcher) {
        suspendCancellableCoroutine { continuation ->
            parser.onFinish(object : OnTaskCompleted {
                override fun onTaskCompleted(channel: Channel) {
                    continuation.resume(channel)
                }

                override fun onError(e: Exception) {
                    continuation.resumeWithException(e)
                }
            })
            parser.execute(url)
            continuation.invokeOnCancellation { parser.cancel() }
        }
    }
}
