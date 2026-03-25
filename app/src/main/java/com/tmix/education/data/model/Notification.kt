package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Notification entity matching Backend NotificationEntity
 */
data class Notification(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String = "general", // general, payment, attendance, new_registration, etc.

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("recipientId")
    val recipientId: String,

    @SerializedName("isRead")
    val isRead: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Unread count response
 */
data class UnreadCountResponse(
    @SerializedName("count")
    val count: Int = 0
)

/**
 * Notification list response (paginated)
 */
data class NotificationListResponse(
    @SerializedName("result")
    val result: List<Notification> = emptyList(),

    @SerializedName("meta")
    val meta: PaginationMeta? = null
)
