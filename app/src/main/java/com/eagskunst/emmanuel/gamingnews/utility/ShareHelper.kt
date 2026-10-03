package com.eagskunst.emmanuel.gamingnews.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.eagskunst.emmanuel.gamingnews.R

/**
 * Shares an article's original link through Android's share sheet with the app's branded
 * message. [title] is optional; when absent or blank only the URL is shared.
 */
fun Context.shareArticle(title: String?, url: String) {
    val text = if (title.isNullOrBlank()) {
        getString(R.string.article_share_text_url_only, url)
    } else {
        getString(R.string.article_share_text, title, url)
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, getString(R.string.article_share_chooser_title))
    if (this !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}
