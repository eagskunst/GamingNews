package com.eagskunst.emmanuel.gamingnews.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun Context.openCustomTab(uri: Uri) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
    if (this !is Activity) {
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    customTabsIntent.launchUrl(this, uri)
}
