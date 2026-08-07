package com.eagskunst.emmanuel.gamingnews.utility

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun Activity.openCustomTab(uri: Uri) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
    customTabsIntent.launchUrl(this, uri)
}
