package com.example.newsrecommendation.model

data class RecommendationItem(
    val title: String,
    val description: String,
    val category: String?,
    val url: String,
    val published_at: String?,
    val embedding_score: Double,
    val keyword_score: Int,
    val hybrid_score: Double,
    val personalized_score: Double
)