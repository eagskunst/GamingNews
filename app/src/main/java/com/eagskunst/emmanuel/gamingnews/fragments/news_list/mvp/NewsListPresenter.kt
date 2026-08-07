package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp

import android.text.Html
import android.util.Log
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.utility.SimpleDateSingleton
import com.prof.rssparser.Article
import com.prof.rssparser.Channel
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by eagskunst on 09/01/2019
 */
class NewsListPresenter(private val model: NewsListModel) :
    NewsListView.Presenter, NewsListView.OnArticlesLoadedListener {

    private var view: NewsListView.View? = null
    private var list: MutableList<NewsModel> = ArrayList()
    private val titles: MutableSet<String> = HashSet()
    private var length = 0

    init {
        model.setListener(this)
    }

    override fun onCreateView(view: NewsListView.View) {
        this.view = view
        this.list = ArrayList()
    }

    override fun onDestroyView() {
        this.view = null
    }

    override fun getArticles(urls: Array<String>) {
        if (view!!.checkInternetConnection()) {
            this.length = urls.size - 1
            for (url in urls) {
                Log.d("NewListPresenter", "Getting from url$url")
                model.getArticlesFromUrl(url)
            }
        } else {
            view!!.createAlertDialog(R.string.check_your_connection)
            view!!.updateList(list)
        }
    }

    override fun formatDescription(content: String): String {
        val mString = StringBuilder()
        var text = content
        if (text.isNotEmpty()) {
            text = Html.fromHtml(text).toString().replace(65532.toChar(), ' ').trim()
            if (text.indexOf('.') != -1) {
                mString.append(text.substring(0, text.indexOf('.')))
                mString.append('.')
            } else {
                mString.append(text)
                mString.append("...")
            }
            if (mString.length > 180) {
                mString.delete(180, mString.length - 1)
                mString.append("...")
            }
        }
        return mString.toString()
    }

    override fun sortListByDate() {
        list.sortWith(Comparator { newsModel, t1 ->
            if (newsModel.pubDate != null && t1.pubDate != null) {
                t1.pubDate.compareTo(newsModel.pubDate)
            } else {
                1
            }
        })
        view!!.updateList(list)
    }

    override fun onGetArticlesSuccess(articles: List<Article>, channel: Channel) {
        val sdf: SimpleDateFormat = SimpleDateSingleton.getInstance().inputSdf
        for (article in articles) {
            if (!titles.contains(article.title)) {
                val mDate = parseDate(article.pubDate, sdf)
                val mDescription = article.description ?: ""
                val newsModel = NewsModel(
                    article.image ?: "", article.title ?: "",
                    formatDescription(mDescription), article.link ?: "", mDate, channel.title ?: ""
                )
                list.add(newsModel)
                titles.add(article.title ?: "")
            }
        }
        length--
        if (length == 0) {
            sortListByDate()
        }
    }

    private fun parseDate(pubDate: String?, sdf: SimpleDateFormat): Date {
        return try {
            sdf.parse(pubDate)
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }

    override fun onGetArticlesFail() {
        view!!.showToastError(R.string.cant_get_articles)
        length--
        if (length == 0) {
            sortListByDate()
        }
    }
}
