package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTopicsDataSource @Inject constructor(
    private val messaging: FirebaseMessaging
) {

    suspend fun subscribeToTopic(topic: String) {
        messaging.subscribeToTopic(topic).await()
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic).await()
    }
}
