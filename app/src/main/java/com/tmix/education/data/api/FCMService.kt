package com.tmix.education.data.api

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tmix.education.data.model.Notification
import com.tmix.education.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging service
 * Handles incoming push notifications and token refresh
 */
class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Called when a new FCM token is generated
     * Send it to the backend to register this device
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        sendTokenToServer(token)
    }

    /**
     * Called when a push notification is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        // Handle data payload (sent from our backend)
        val data = message.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data: $data")
            val title = data["title"] ?: "Thông báo mới"
            val body = data["message"] ?: data["body"] ?: ""
            val notificationId = data["notificationId"]

            NotificationHelper.showNotification(
                context = this,
                title = title,
                message = body,
                notificationId = notificationId?.hashCode() ?: System.currentTimeMillis().toInt()
            )
        }

        // Handle notification payload (if sent via Firebase Console)
        message.notification?.let { notification ->
            NotificationHelper.showNotification(
                context = this,
                title = notification.title ?: "Thông báo mới",
                message = notification.body ?: "",
            )
        }
    }

    /**
     * Send FCM token to backend for this user.
     * Only sends if the user is currently logged in.
     */
    private fun sendTokenToServer(token: String) {
        // Only register if user is logged in
        val tokenManager = ApiConfig.getTokenManager()
        if (tokenManager?.isLoggedInSync() != true) {
            Log.d(TAG, "User not logged in, skipping FCM token registration")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.registerDeviceToken(
                    mapOf(
                        "token" to token,
                        "platform" to "android"
                    )
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token registered successfully")
                } else {
                    Log.w(TAG, "Failed to register FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }
}
