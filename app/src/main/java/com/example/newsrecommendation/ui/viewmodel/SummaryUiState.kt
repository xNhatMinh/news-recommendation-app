package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.SummarySource

data class SummaryUiState(
    val selectedTopic: String = "",
    val isLoading: Boolean = false,
    val summary: String = "",
    val sources: List<SummarySource> = emptyList(),
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)