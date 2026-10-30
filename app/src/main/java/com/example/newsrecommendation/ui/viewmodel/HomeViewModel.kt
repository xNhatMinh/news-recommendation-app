package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.RecommendationRequest
import com.example.newsrecommendation.storage.TokenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val pageSize = 20
    private var nextOffset = 0
    private var checkingNotificationCount = false
    private var hasLoaded = false
    private var loadJob: Job? = null

    var uiState by mutableStateOf(HomeUiState())
        private set

    fun loadNotificationCount() {
        if (checkingNotificationCount) return
        checkingNotificationCount = true
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
                if (token.isNullOrBlank()) return@launch
                val response = RetrofitClient.apiService.getNotificationCounts("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        uiState = uiState.copy(unreadNotificationCount = data.unread_count)
                    }
                } else if (response.code() == 401) {
                    uiState = uiState.copy(needsLogin = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Chuông sẽ kiểm tra lại khi màn hình đang mở.
            } finally {
                checkingNotificationCount = false
            }
        }
    }

    fun loadInitialNews() {
        if (!hasLoaded && !uiState.isLoading && !uiState.isLoadingMore) {
            loadNews()
        }
    }

    fun onPreferencesOpened() {
        if (uiState.selectedFilter == "Dành cho bạn") {
            loadJob?.cancel()
            hasLoaded = false
            uiState = uiState.copy(isLoading = false, isLoadingMore = false)
        }
    }

    fun loadNews(filter: String = uiState.selectedFilter) {
        fetchArticles(filter, append = false)
    }

    fun loadMore() {
        if (uiState.isLoading || uiState.isLoadingMore || !uiState.hasMore) return
        fetchArticles(uiState.selectedFilter, append = true)
    }

    private fun setError(message: String, needsLogin: Boolean = false) {
        uiState = uiState.copy(
            isLoading = false,
            isLoadingMore = false,
            errorMessage = message,
            needsLogin = needsLogin
        )
    }

    private fun handleHttpError(code: Int) {
        if (code == 401) {
            setError("Phiên không còn hợp lệ, hãy đăng nhập lại", needsLogin = true)
        } else {
            setError("Không thể tải tin, vui lòng thử lại")
        }
    }

    private fun fetchArticles(filter: String, append: Boolean) {
        loadJob?.cancel()

        if (append) {
            uiState = uiState.copy(isLoadingMore = true, errorMessage = "")
        } else {
            nextOffset = 0
            hasLoaded = false
            uiState = uiState.copy(
                selectedFilter = filter,
                isLoading = true,
                isLoadingMore = false,
                articles = emptyList(),
                hasMore = false,
                errorMessage = "",
                needsLogin = false
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setError("Bạn cần đăng nhập lại", needsLogin = true)
                    return@launch
                }

                if (filter == "Dành cho bạn") {
                    loadRecommendations(token)
                } else {
                    loadLatestArticles(token, filter, append)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể tải tin, vui lòng thử lại")
            }
        }
    }

    private suspend fun loadRecommendations(token: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -29)

        val response = RetrofitClient.apiService.getRecommendations(
            authorization = "Bearer $token",
            request = RecommendationRequest(
                start_date = dateFormat.format(calendar.time),
                end_date = endDate
            )
        )
        val data = response.body()

        if (response.isSuccessful && data != null) {
            uiState = uiState.copy(
                articles = data.results,
                isLoading = false,
                isLoadingMore = false,
                hasMore = false,
                needsLogin = false
            )
            hasLoaded = true
        } else {
            handleHttpError(response.code())
        }
    }

    private suspend fun loadLatestArticles(token: String, filter: String, append: Boolean) {
        val offset = if (append) nextOffset else 0
        val response = RetrofitClient.apiService.getLatestArticles(
            authorization = "Bearer $token",
            limit = pageSize,
            offset = offset,
            category = if (filter == "Mới nhất") null else filter
        )
        val data = response.body()

        if (response.isSuccessful && data != null) {
            val articles = if (append) uiState.articles + data.results else data.results
            nextOffset = offset + data.results.size
            uiState = uiState.copy(
                articles = articles.distinctBy { it.url },
                categories = data.categories,
                isLoading = false,
                isLoadingMore = false,
                hasMore = data.has_more,
                needsLogin = false
            )
            hasLoaded = true
        } else {
            handleHttpError(response.code())
        }
    }
}
