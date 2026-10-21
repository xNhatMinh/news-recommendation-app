package com.example.newsrecommendation.api
import retrofit2.http.*
import retrofit2.Response
import com.example.newsrecommendation.model.*
import com.example.newsrecommendation.model.UserResponse
import retrofit2.http.Header


data class HealthResponse(
    val status: String
)

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("recommend")
    suspend fun getRecommendations(
        @Header("Authorization") authorization: String,
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

    @GET("preferences")
    suspend fun getPreferences(
        @Header("Authorization") authorization: String
    ): Response<PreferencesResponse>

    @PUT("preferences")
    suspend fun savePreferences(
        @Header("Authorization") authorization: String,
        @Body request: PreferencesRequest
    ): Response<SavePreferencesResponse>
}