package com.example.newsrecommendation.api
import retrofit2.http.*
import retrofit2.Response
import com.example.newsrecommendation.model.*
import com.example.newsrecommendation.model.UserResponse
import com.example.newsrecommendation.model.ArticleDetail
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

    @POST("auth/google")
    suspend fun loginGoogle(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: PasswordResetRequest
    ): Response<PasswordResetResponse>

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

    @POST("devices")
    suspend fun saveDeviceToken(
        @Header("Authorization") authorization: String,
        @Body request: DeviceTokenRequest
    ): Response<DeviceTokenResponse>

    @POST("devices/remove")
    suspend fun removeDeviceToken(
        @Header("Authorization") authorization: String,
        @Body request: DeviceTokenRequest
    ): Response<DeviceTokenResponse>

    @POST("summary")
    suspend fun createSummary(
        @Header("Authorization") authorization: String,
        @Body request: SummaryRequest
    ): Response<SummaryResponse>

    @GET("articles/latest")
    suspend fun getLatestArticles(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("category") category: String? = null
    ): Response<LatestArticlesResponse>

    @GET("articles/search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<SearchArticlesResponse>

    @GET("bookmarks")
    suspend fun getBookmarks(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<SavedArticlesResponse>

    @GET("bookmarks/status")
    suspend fun getBookmarkStatus(
        @Header("Authorization") authorization: String,
        @Query("url") url: String
    ): Response<BookmarkStatus>

    @PUT("bookmarks")
    suspend fun saveBookmark(
        @Header("Authorization") authorization: String,
        @Body request: BookmarkRequest
    ): Response<BookmarkStatus>

    @DELETE("bookmarks")
    suspend fun removeBookmark(
        @Header("Authorization") authorization: String,
        @Query("url") url: String
    ): Response<BookmarkStatus>

    @GET("articles/detail")
    suspend fun getArticleDetail(
        @Query("url") url: String
    ): Response<ArticleDetail>
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("unread_only") unreadOnly: Boolean = false
    ): Response<NotificationsResponse>

    @GET("notifications/unread-count")
    suspend fun getNotificationCounts(
        @Header("Authorization") authorization: String
    ): Response<NotificationCounts>

    @PATCH("notifications/{id}/read")
    suspend fun readNotification(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<NotificationCounts>

    @PATCH("notifications/read-all")
    suspend fun readAllNotifications(
        @Header("Authorization") authorization: String
    ): Response<NotificationCounts>
}