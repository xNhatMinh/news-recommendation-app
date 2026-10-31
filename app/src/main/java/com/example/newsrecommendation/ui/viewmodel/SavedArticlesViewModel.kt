package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val pageSize = 20
    private var nextOffset = 0
    private var loadJob: Job? = null

    var uiState by mutableStateOf(SavedArticlesUiState())
        private set

    fun loadArticles() {
        fetchArticles(append = false)
    }

    fun retry() {
        fetchArticles(append = nextOffset > 0)
    }

    fun loadMore() {
        if (uiState.isLoading || uiState.isLoadingMore || !uiState.hasMore) return
        fetchArticles(append = true)
    }

    private fun showError(message: String, needsLogin: Boolean = false) {
        uiState = uiState.copy(
            isLoading = false,
            isLoadingMore = false,
            errorMessage = message,
            needsLogin = needsLogin
        )
    }

    private fun fetchArticles(append: Boolean) {
        loadJob?.cancel()
        val offset = if (append) nextOffset else 0
        uiState = if (append) {
            uiState.copy(isLoadingMore = true, errorMessage = "")
        } else {
            nextOffset = 0
            SavedArticlesUiState(isLoading = true)
        }
        loadJob = viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
                if (token.isNullOrBlank()) {
                    showError("Bạn cần đăng nhập lại", needsLogin = true)
                    return@launch
                }
                val response = RetrofitClient.apiService.getBookmarks("Bearer $token", pageSize, offset)
                val data = response.body()
                if (response.isSuccessful && data != null) {
                    nextOffset = offset + data.results.size
                    val articles = if (append) uiState.articles + data.results else data.results
                    uiState = uiState.copy(
                        articles = articles.distinctBy { it.url },
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = data.has_more
                    )
                } else if (response.code() == 401) {
                    showError("Phiên đã hết hạn, hãy đăng nhập lại", needsLogin = true)
                } else {
                    showError("Không thể tải bài đã lưu, vui lòng thử lại")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showError("Không thể kết nối máy chủ, vui lòng thử lại")
            }
        }
    }
}
