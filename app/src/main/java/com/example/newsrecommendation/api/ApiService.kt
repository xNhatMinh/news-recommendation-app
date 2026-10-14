package com.example.newsrecommendation.api
import com.example.newsrecommendation.model.RecommendationResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.Response


data class HealthResponse(
    val status: String
)

data class RecommendationRequest(
    val user_id: Int,
    val start_date: String,
    val end_date: String,
    val top_k: Int = 10
)

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendationRequest
    ): Response<RecommendationResponse>
}