package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp

import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.prof.rssparser.Article
import com.prof.rssparser.Channel

/**
 * Created by eagskunst on 09/01/2019
 */
interface NewsListView {

    interface Presenter {
        fun onCreateView(view: View)
        fun onDestroyView()
        fun getArticles(urls: Array<String>)
        fun formatDescription(content: String): String
        fun sortListByDate()
    }

    interface OnArticlesLoadedListener {
        fun onGetArticlesSuccess(articles: List<Article>, channel: Channel)
        fun onGetArticlesFail()
    }

    interface View {
        fun updateList(newsList: List<NewsModel>)
        fun getArticleList()
        fun checkInternetConnection(): Boolean
        fun createAlertDialog(message: Int)
        fun showToastError(message: String)
        fun showToastError(message: Int)
        fun loadListFromSharedPreferences()
    }
}
