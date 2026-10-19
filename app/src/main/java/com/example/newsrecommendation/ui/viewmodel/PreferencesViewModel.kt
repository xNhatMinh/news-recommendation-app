package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.newsrecommendation.storage.TokenManager
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.newsrecommendation.model.PreferencesRequest

class PreferencesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(PreferencesUiState())
        private set

    val topics = listOf(
        "Thời sự",
        "Thế giới",
        "Kinh doanh",
        "Khoa học",
        "Thể thao",
        "Giải trí",
        "Sức khỏe"
    )

    fun toggleTopic(topic: String) {
        if(uiState.isLoading) return

        val topics = uiState.selectedTopics

        val newTopics = if(topic in topics) {
            topics - topic
        } else {
            topics + topic
        }

        uiState = uiState.copy(
            selectedTopics = newTopics,
            errorMessage = "",
            isSaved =  false
        )

    }

    private fun setLoading(isLoading: Boolean) {
        uiState = uiState.copy(
            isLoading = isLoading,
            errorMessage = "",
            isSaved = false
        )
    }

    private fun setError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            errorMessage = message,
            isSaved = false
        )
    }

    fun loadPreferences() {
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

                val response = RetrofitClient.apiService
                    .getPreferences("Bearer $token")

                val data = response.body()

                if (response.isSuccessful && data != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        selectedTopics = data.preferences.map { it.topic },
                        isSaved = false
                    )
                } else if (response.code() == 401) {
                    setError("Phiên không còn hợp lệ, hãy đăng nhập lại")
                } else {
                    setError("Không thể tải chủ đề")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể tải chủ đề, vui lòng thử lại")
            }
        }
    }

    fun savePreferences() {
        if (uiState.isLoading) return

        val request = PreferencesRequest(
            topics = uiState.selectedTopics
        )

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

                val response = RetrofitClient.apiService.savePreferences(
                    authorization = "Bearer $token",
                    request = request
                )

                val data = response.body()

                when {
                    response.isSuccessful && data != null -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            selectedTopics = data.topics,
                            isSaved = true
                        )
                    }

                    response.code() == 401 -> {
                        setError("Phiên không còn hợp lệ, hãy đăng nhập lại")
                    }

                    else -> {
                        setError("Không thể lưu chủ đề")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể lưu chủ đề, vui lòng thử lại")
            }
        }
    }
}
