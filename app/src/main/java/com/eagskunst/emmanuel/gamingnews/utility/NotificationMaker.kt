package com.eagskunst.emmanuel.gamingnews.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.views.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Locale

class NotificationMaker : FirebaseMessagingService() {
    private val TAG = javaClass.simpleName
    private var sessionToken: String? = null

    @JvmField
    var sharedPreferences: SharedPreferences? = null

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d(TAG, "New token: $s")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message: $remoteMessage")
        val lang = remoteMessage.data["lang"]
        if (lang == Locale.getDefault().language) {
            inboxStyle.addLine(remoteMessage.data["descp"]?.replace("_", " "))
        }
        generateNotification(remoteMessage.data["title"], remoteMessage.data["descp"], lang)
    }

    fun generateNotification(title: String?, body: String?, lang: String?) {
        Log.d(TAG, "Lang: $lang")
        if (lang == Locale.getDefault().language) {
            val pendingIntent = Intent(this, MainActivity::class.java)
            pendingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val requestCode = 0
            val notifyPendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                pendingIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "channelID")
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(notifyPendingIntent)
                .setColor(getColor(R.color.colorPrimary))
                .setStyle(inboxStyle)
                .setSmallIcon(R.drawable.ic_all)
                .build()

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createChannel(manager)
            manager.notify(requestCode, notification)
        }
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < 26) return
        val channel = NotificationChannel("channelID", "Articles channel", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Articles channel"
        manager.createNotificationChannel(channel)
    }

    fun setToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { updatedToken ->
            if (SharedPreferencesLoader.getFirebaseToken(sharedPreferences!!) == "no_token") {
                sessionToken = updatedToken
                SharedPreferencesLoader.saveFirebaseToken(sharedPreferences!!.edit(), sessionToken!!)
                Log.e("Updated Token", updatedToken)
            } else {
                Log.e(TAG, "Token has already been created")
            }
        }
    }

    fun setSessionToken(sessionToken: String) {
        this.sessionToken = sessionToken
    }

    fun getSessionToken(): String? {
        return sessionToken
    }

    companion object {
        @JvmField
        var inboxStyle = NotificationCompat.InboxStyle()
    }
}
