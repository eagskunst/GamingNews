package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp

import android.util.Log
import com.prof.rssparser.Channel
import com.prof.rssparser.OnTaskCompleted
import com.prof.rssparser.Parser

/**
 * Created by eagskunst on 09/01/2019
 */
class NewsListModel : OnTaskCompleted {

    private var listener: NewsListView.OnArticlesLoadedListener? = null

    fun getArticlesFromUrl(url: String) {
        val parser = Parser.Builder().build()
        parser.onFinish(this)
        parser.execute(url)
    }

    override fun onTaskCompleted(channel: Channel) {
        Log.d("NewsListModel", "onTaskCompleted: parsed!")
        listener!!.onGetArticlesSuccess(channel.articles, channel)
    }

    override fun onError(e: Exception) {
        listener!!.onGetArticlesFail()
        Log.e("NewListModel", "onError: Error parsing" + e.message, e)
    }

    fun setListener(listener: NewsListView.OnArticlesLoadedListener) {
        this.listener = listener
    }
}
