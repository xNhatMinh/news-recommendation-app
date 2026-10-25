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

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(HistoryUiState())
        private set

    private fun setError(
        message: String,
        needsLogin: Boolean = false
    ) {
        uiState = HistoryUiState(
            errorMessage = message,
            needsLogin = needsLogin
        )
    }

    fun loadHistory() {
        if (uiState.isLoading) return

        uiState = HistoryUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setError("Bạn cần đăng nhập lại", needsLogin = true)
                    return@launch
                }

                val response = RetrofitClient.apiService.getHistory(
                    authorization = "Bearer $token"
                )

                val data = response.body()

                when {
                    response.isSuccessful && data != null -> {
                        uiState = HistoryUiState(
                            history = data.history
                        )
                    }

                    response.code() == 401 -> {
                        setError(
                            "Phiên hết hạn, hãy đăng nhập lại",
                            needsLogin = true
                        )
                    }

                    else -> {
                        setError("Không thể tải lịch sử")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Chưa tải được lịch sử, vui lòng thử lại")
            }
        }
    }
}