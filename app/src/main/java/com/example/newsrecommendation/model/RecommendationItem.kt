package com.example.newsrecommendation.model

data class RecommendationItem(
    val title: String,
    val description: String,
    val category: String?,
    val url: String,
    val image_url: String? = null,
    val published_at: String?,
    val embedding_score: Double = 0.0,
    val keyword_score: Int = 0,
    val hybrid_score: Double = 0.0,
    val personalized_score: Double = 0.0
)