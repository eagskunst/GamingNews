package com.eagskunst.emmanuel.gamingnews.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader

/**
 * Created by eagskunst on 09/11/2018
 */
class SaveArticleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val savedList = SharedPreferencesLoader.retrieveList(sharedPreferences).toMutableList()
        val b = intent.getBundleExtra("Bundle")
        var article: NewsModel? = b?.getParcelable("Article")
        Log.d(SaveArticleReceiver::class.java.simpleName, "onReceive: entered")
        var isSaved = false
        for (n in savedList) {
            n.link
            article?.link
            if (n.link == article?.link) {
                article = n
                isSaved = true
            }
        }
        if (!isSaved) {
            savedList.add(0, article!!)
            Toast.makeText(context, R.string.article_saved, Toast.LENGTH_SHORT).show()
        } else {
            savedList.remove(article)
            Toast.makeText(context, R.string.article_removed, Toast.LENGTH_SHORT).show()
        }
        SharedPreferencesLoader.saveList(sharedPreferences.edit(), savedList)
    }
}
