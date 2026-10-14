package com.example.newsrecommendation.model;

data class RecommendationResponse(
    val user_id: Int,
    val start_date: String,
    val end_date: String,
    val results: List<RecommendationItem>
)