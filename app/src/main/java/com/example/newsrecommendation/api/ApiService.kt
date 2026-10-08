package com.example.newsrecommendation.api

import retrofit2.Response
import retrofit2.http.GET

data class HealthResponse(
    val status: String
)

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}