package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.RecommendationItem

data class HomeUiState(
    val selectedFilter: String = "Mới nhất",
    val unreadNotificationCount: Int = 0,
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val articles: List<RecommendationItem> = emptyList(),
    val hasMore: Boolean = false,
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)
