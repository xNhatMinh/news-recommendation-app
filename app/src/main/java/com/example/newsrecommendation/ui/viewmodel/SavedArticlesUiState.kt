package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.RecommendationItem

data class SavedArticlesUiState(
    val articles: List<RecommendationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)
