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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(ArticleDetailUiState())
        private set

    private fun setError(
        message: String,
        needsLogin: Boolean = false
    ) {
        uiState = ArticleDetailUiState(
            errorMessage = message,
            needsLogin = needsLogin
        )
    }

    fun loadArticle(url: String) {
        if (uiState.isLoading) return

        uiState = ArticleDetailUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setError(
                        message = "Bạn cần đăng nhập lại",
                        needsLogin = true
                    )
                    return@launch
                }

                val response = RetrofitClient.apiService.getArticleDetail(
                    authorization = "Bearer $token",
                    url = url
                )

                val article = response.body()

                when {
                    response.isSuccessful && article != null -> {
                        uiState = ArticleDetailUiState(
                            article = article
                        )
                    }

                    response.code() == 401 -> {
                        setError(
                            message = "Phiên đăng nhập đã hết hạn",
                            needsLogin = true
                        )
                    }

                    response.code() == 404 -> {
                        setError("Không tìm thấy bài báo")
                    }

                    else -> {
                        setError("Không thể tải nội dung bài báo")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể kết nối, vui lòng thử lại")
            }
        }
    }
}