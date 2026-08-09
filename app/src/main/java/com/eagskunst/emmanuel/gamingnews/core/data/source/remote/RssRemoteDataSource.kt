package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RssRemoteDataSource @Inject constructor(
    private val dispatchers: DispatcherProvider
) {

    suspend fun fetchChannel(url: String): RssChannel = withContext(dispatchers.io) {
        val parser = RssParser()
        parser.getRssChannel(url)
    }
}
