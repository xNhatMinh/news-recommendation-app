package com.example.newsrecommendation.model

data class LoginResponse(
    val user_id: Int,
    val email: String,
    val message: String
)