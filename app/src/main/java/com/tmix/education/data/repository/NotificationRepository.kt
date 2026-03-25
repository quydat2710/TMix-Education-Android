package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.model.*
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Notification operations
 */
class NotificationRepository(
    private val apiService: ApiService = ApiConfig.getApiService()
) {

    /**
     * Get paginated notifications for the current user
     */
    suspend fun getNotifications(
        page: Int = 1,
        limit: Int = 20
    ): Result<NotificationListResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("NotificationRepo", "Fetching notifications page=$page limit=$limit")
            val response = apiService.getNotifications(page, limit)
            Log.d("NotificationRepo", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            Log.d("NotificationRepo", "Response body: ${response.body()}")
            Log.d("NotificationRepo", "Response errorBody: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                Log.d("NotificationRepo", "Got ${data.result.size} notifications")
                Result.success(data)
            } else {
                Log.w("NotificationRepo", "No data in response, returning empty list")
                Result.success(NotificationListResponse())
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to fetch notifications", e)
            Result.failure(e)
        }
    }

    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("NotificationRepo", "Fetching unread count")
            val response = apiService.getUnreadCount()
            Log.d("NotificationRepo", "Unread count response code: ${response.code()}, body: ${response.body()}")

            if (response.isSuccessful && response.body()?.data != null) {
                val count = response.body()!!.data!!.count
                Log.d("NotificationRepo", "Unread count: $count")
                Result.success(count)
            } else {
                Log.w("NotificationRepo", "Unread count: no data, returning 0")
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to fetch unread count", e)
            Result.failure(e)
        }
    }

    /**
     * Mark a specific notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markNotificationAsRead(notificationId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllNotificationsAsRead()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteNotification(notificationId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
