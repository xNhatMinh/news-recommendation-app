package com.example.newsrecommendation.ui.viewmodel

import com.example.newsrecommendation.model.ArticleDetail

data class ArticleDetailUiState(
    val isLoading: Boolean = false,
    val article: ArticleDetail? = null,
    val errorMessage: String = "",
    val needsLogin: Boolean = false
)