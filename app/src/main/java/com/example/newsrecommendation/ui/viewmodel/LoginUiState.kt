package com.example.newsrecommendation.ui.viewmodel

data class LoginUiState(
    val isLoading: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = "",
    val isLoggedIn: Boolean = false
)