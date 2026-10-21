package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.RecommendationItem

data class HomeUiState(
    val isLoading: Boolean = false,
    val articles: List<RecommendationItem> = emptyList(),
    val errorMessage: String = ""
)