package com.example.newsrecommendation.model

data class RecommendationRequest(
    val start_date: String,
    val end_date: String,
    val top_k: Int = 10
)