package com.example.newsrecommendation.model

data class HistoryItem(
    val id: Long,
    val title: String,
    val url: String,
    val created_at: String
)