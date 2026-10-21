package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.newsrecommendation.storage.TokenManager
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.RecommendationRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(HomeUiState())
        private set

    private fun setLoading(isLoading: Boolean) {
        uiState = uiState.copy(
            isLoading = isLoading,
            errorMessage = ""
        )
    }

    private fun setError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun loadRecommendations(startDate: String, endDate: String) {
        if (uiState.isLoading) return
        setLoading(true)

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setError("Bạn cần đăng nhập lại")
                    return@launch
                }

                val request = RecommendationRequest(
                    start_date = startDate,
                    end_date = endDate
                )

                val response = RetrofitClient.apiService.getRecommendations(
                    authorization = "Bearer $token",
                    request = request
                )

                val data = response.body()

                if (response.isSuccessful && data != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        articles = data.results
                    )
                } else if (response.code() == 401) {
                    setError("Phiên không còn hợp lệ, hãy đăng nhập lại")
                } else {
                    setError("Không thể tải tin")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể tải tin, vui lòng thử lại")
            }
        }
    }
}