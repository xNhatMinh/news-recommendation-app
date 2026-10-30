package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.NotificationItem

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val unreadOnly: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isUpdating: Boolean = false,
    val openingId: Long? = null,
    val hasMore: Boolean = false,
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)
