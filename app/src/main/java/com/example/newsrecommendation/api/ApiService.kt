package com.example.newsrecommendation.api
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.Response
import com.example.newsrecommendation.model.*
import com.example.newsrecommendation.model.UserResponse
import retrofit2.http.Header


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

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ) : Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ) : Response<UserResponse>
}