package com.example.newsrecommendation.model

data class LoginResponse(
    val user_id: Int,
    val email: String,
    val message: String,
    val access_token: String,
    val token_type: String
)