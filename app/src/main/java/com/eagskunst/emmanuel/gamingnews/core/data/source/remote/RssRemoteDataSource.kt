package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.prof.rssparser.Channel
import com.prof.rssparser.OnTaskCompleted
import com.prof.rssparser.Parser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RssRemoteDataSource @Inject constructor(
    private val dispatchers: DispatcherProvider
) {

    suspend fun fetchChannel(url: String): Channel = withContext(dispatchers.io) {
        val parser = Parser.Builder().build()
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
