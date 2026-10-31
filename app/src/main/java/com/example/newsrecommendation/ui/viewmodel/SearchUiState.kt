package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.RecommendationItem

data class SearchUiState(
    val query: String = "",
    val articles: List<RecommendationItem> = emptyList(),
    val hasSearched: Boolean = false,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)
