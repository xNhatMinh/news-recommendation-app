package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.HistoryItem

data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)