package com.tmix.education.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.NotificationSSEService
import com.tmix.education.data.model.Notification
import com.tmix.education.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for notification state across dashboards
 * Manages unread count, SSE connection, FCM registration, and in-app notification display
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationRepository = NotificationRepository()
    private var sseService: NotificationSSEService? = null

    // Unread count displayed on badge
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // Latest notification from SSE (for Snackbar display)
    private val _latestNotification = MutableStateFlow<Notification?>(null)
    val latestNotification: StateFlow<Notification?> = _latestNotification.asStateFlow()

    // Snackbar visibility
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    init {
        refreshUnreadCount()
        startSSE()
        registerFCMToken()
    }

    /**
     * Fetch unread count from API
     */
    fun refreshUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount()
                .onSuccess { count ->
                    _unreadCount.value = count
                }
        }
    }

    /**
     * Register FCM device token with the backend
     */
    private fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("NotificationVM", "FCM token fetch failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("NotificationVM", "FCM token: $token")

            // Send to backend
            viewModelScope.launch {
                try {
                    val apiService = ApiConfig.getApiService()
                    val response = apiService.registerDeviceToken(
                        mapOf("token" to token, "platform" to "android")
                    )
                    if (response.isSuccessful) {
                        Log.d("NotificationVM", "FCM token registered with backend")
                    } else {
                        Log.w("NotificationVM", "FCM token register failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("NotificationVM", "Error registering FCM token", e)
                }
            }
        }
    }

    /**
     * Start SSE connection for real-time in-app notifications (Snackbar + badge)
     * FCM handles system tray notifications separately
     */
    fun startSSE() {
        val token = ApiConfig.getTokenManager()?.getAccessTokenSync() ?: return

        // Disconnect if already running
        sseService?.disconnect()

        sseService = NotificationSSEService(
            onNotification = { notification ->
                // Increment unread count
                _unreadCount.value = _unreadCount.value + 1
                // Show in-app Snackbar (FCM handles system notification)
                _latestNotification.value = notification
                _showSnackbar.value = true
            }
        )
        sseService?.connect(token)
    }

    /**
     * Stop SSE connection
     */
    fun stopSSE() {
        sseService?.disconnect()
        sseService = null
    }

    /**
     * Dismiss the Snackbar
     */
    fun dismissSnackbar() {
        _showSnackbar.value = false
    }

    /**
     * Called when user reads a notification — decrement count
     */
    fun onNotificationRead() {
        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
    }

    /**
     * Called when user reads all notifications
     */
    fun onAllNotificationsRead() {
        _unreadCount.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopSSE()
    }
}
