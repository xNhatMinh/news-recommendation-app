package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.SummaryRequest
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(SummaryUiState())
        private set

    fun selectTopic(topic: String) {
        if (uiState.isLoading || topic == uiState.selectedTopic) return

        uiState = SummaryUiState(selectedTopic = topic)
    }

    private fun setError(
        message: String,
        needsLogin: Boolean = false
    ) {
        uiState = uiState.copy(
            isLoading = false,
            errorMessage = message,
            needsLogin = needsLogin
        )
    }

    fun createSummary(startDate: String, endDate: String) {
        if (uiState.isLoading) return

        val topic = uiState.selectedTopic

        if (topic.isBlank()) {
            setError("Hãy chọn một chủ đề")
            return
        }

        uiState = SummaryUiState(
            selectedTopic = topic,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setError("Bạn cần đăng nhập lại", needsLogin = true)
                    return@launch
                }

                val request = SummaryRequest(
                    topic = topic,
                    start_date = startDate,
                    end_date = endDate
                )

                val response = RetrofitClient.apiService.createSummary(
                    authorization = "Bearer $token",
                    request = request
                )

                val data = response.body()

                when {
                    response.isSuccessful && data != null -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            summary = data.summary,
                            sources = data.sources
                        )
                    }

                    response.code() == 401 -> {
                        setError(
                            "Phiên hết hạn, hãy đăng nhập lại",
                            needsLogin = true
                        )
                    }

                    response.code() == 404 -> {
                        setError("Chưa có bài phù hợp để tổng hợp")
                    }

                    else -> {
                        setError("Không thể tổng hợp tin, vui lòng thử lại")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Chưa tổng hợp được tin, vui lòng thử lại")
            }
        }
    }
}