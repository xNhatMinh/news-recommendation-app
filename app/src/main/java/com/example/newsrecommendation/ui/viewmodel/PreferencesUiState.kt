package com.example.newsrecommendation.ui.viewmodel

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val selectedTopics: List<String> = emptyList(),
    val errorMessage: String = "",
    val isSaved: Boolean = false,
    val needsLogin: Boolean = false
)