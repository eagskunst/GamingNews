package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.ui.reader.ReaderActivity
import com.eagskunst.emmanuel.gamingnews.utility.openCustomTab
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OpenArticleUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(url: String, mode: ArticleOpenMode) {
        when (mode) {
            ArticleOpenMode.CUSTOM_TAB -> context.openCustomTab(Uri.parse(url))
            ArticleOpenMode.EXTERNAL_BROWSER -> openExternalBrowser(url)
            ArticleOpenMode.READER_MODE -> context.openCustomTab(Uri.parse(url))
        }
    }

    private fun openExternalBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooser = Intent.createChooser(intent, null)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            context.openCustomTab(Uri.parse(url))
        }
    }

    private fun openReaderMode(url: String) {
        context.startActivity(
            ReaderActivity.newIntent(context, url)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
