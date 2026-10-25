package com.example.newsrecommendation.model

data class HistoryResponse(
    val user_id: Long,
    val history: List<HistoryItem>
)