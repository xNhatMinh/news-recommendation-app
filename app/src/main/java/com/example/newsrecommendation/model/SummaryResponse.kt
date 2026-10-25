package com.example.newsrecommendation.model

data class SummaryResponse(
    val summary: String,
    val sources: List<SummarySource>
)