package com.example.newsrecommendation.ui.viewmodel

data class RegisterUiState(
    val isLoading: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)