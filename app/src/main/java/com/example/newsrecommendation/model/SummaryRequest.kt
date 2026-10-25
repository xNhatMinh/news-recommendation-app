package com.example.newsrecommendation.model

data class SummaryRequest(
    val topic: String,
    val start_date: String,
    val end_date: String
)