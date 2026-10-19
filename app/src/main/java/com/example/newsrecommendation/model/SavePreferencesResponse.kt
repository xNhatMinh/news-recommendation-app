package com.example.newsrecommendation.model

data class SavePreferencesResponse(
    val message: String,
    val topics: List<String>
)