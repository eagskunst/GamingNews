package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseTopicsDataSource(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {

    suspend fun subscribeToTopic(topic: String) {
        messaging.subscribeToTopic(topic).await()
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic).await()
    }
}
