package com.example.newsrecommendation.model

data class SearchArticlesResponse(
    val results: List<RecommendationItem>,
    val has_more: Boolean
)
