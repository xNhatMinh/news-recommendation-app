package com.example.newsrecommendation.model

data class LatestArticlesResponse(
    val results: List<RecommendationItem>,
    val categories: List<String>,
    val has_more: Boolean
)
