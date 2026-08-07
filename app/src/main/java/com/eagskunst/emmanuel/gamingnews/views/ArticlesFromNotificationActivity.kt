package com.eagskunst.emmanuel.gamingnews.views

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.NewsListFragment
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity

class ArticlesFromNotificationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_from_notification)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        showToolbar(toolbar, R.string.notification, true, null)
        callLog(TAG, "Title: " + supportActionBar!!.title.toString())
        val fakeUrls = arrayOf(TAG)
        supportFragmentManager.beginTransaction()
            .replace(R.id.notificationContainer, NewsListFragment.newInstance(fakeUrls))
            .commit()
    }

    companion object {
        private const val TAG = "ArticlesFromNotification"
    }
}
