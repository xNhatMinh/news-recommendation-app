package com.example.newsrecommendation.model

data class NotificationItem(
    val id: Long,
    val topic: String,
    val title: String,
    val description: String,
    val url: String,
    val image_url: String?,
    val is_read: Boolean,
    val created_at: String
)

data class NotificationCounts(val total_count: Int, val unread_count: Int)

data class NotificationsResponse(
    val results: List<NotificationItem>,
    val total_count: Int,
    val unread_count: Int,
    val has_more: Boolean
)
