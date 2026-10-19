package com.example.newsrecommendation.model

data class PreferencesResponse(
    val user_id: Int,
    val preferences: List<PreferenceItem>
)