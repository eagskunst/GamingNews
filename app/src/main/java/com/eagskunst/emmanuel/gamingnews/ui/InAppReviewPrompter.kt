package com.eagskunst.emmanuel.gamingnews.ui

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reviewPromptDataStore by preferencesDataStore(name = "review_prompt")

/**
 * Asks for a Play in-app review after enough engagement. Play decides whether the dialog is
 * actually shown (its own quota), so a prompt is never treated as a submitted review.
 */
@Singleton
class InAppReviewPrompter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.reviewPromptDataStore

    suspend fun onArticleOpened() {
        store.edit { it[ARTICLES_OPENED] = (it[ARTICLES_OPENED] ?: 0) + 1 }
    }

    /** Call from a resumed Activity, e.g. when the user returns from reading an article. */
    suspend fun maybePrompt(activity: Activity, now: Long = System.currentTimeMillis()) {
        val prefs = store.data.first()
        val installedAt = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        val lastShownAt = prefs[LAST_SHOWN_AT]
        val eligible = (prefs[ARTICLES_OPENED] ?: 0) >= MIN_ARTICLES_OPENED &&
            now - installedAt >= MIN_INSTALL_AGE_MS &&
            (lastShownAt == null || now - lastShownAt >= COOLDOWN_MS)
        if (!eligible) return

        val manager = ReviewManagerFactory.create(activity)
        val reviewInfo = manager.requestReviewFlow().await()
        // Recorded before launching: the launch task completes whether or not Play shows the dialog.
        store.edit { it[LAST_SHOWN_AT] = now }
        manager.launchReviewFlow(activity, reviewInfo).await()
    }

    private companion object {
        const val MIN_ARTICLES_OPENED = 5
        val MIN_INSTALL_AGE_MS = TimeUnit.DAYS.toMillis(3)
        val COOLDOWN_MS = TimeUnit.DAYS.toMillis(30)
        val ARTICLES_OPENED = intPreferencesKey("articles_opened")
        val LAST_SHOWN_AT = longPreferencesKey("last_shown_at")
    }
}
