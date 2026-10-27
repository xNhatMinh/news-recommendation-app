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
import com.example.newsrecommendation.model.NewsTopics

class PreferencesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf(PreferencesUiState())
        private set

    val topics = NewsTopics.all

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

    fun clearTopics() {
        if (uiState.isLoading) return
        uiState = uiState.copy(
            selectedTopics = uiState.selectedTopics.filterNot { it in topics },
            errorMessage = "",
            isSaved = false
        )
    }

    fun addKeyword(text: String): Boolean {
        if (uiState.isLoading) return false
        val keyword = text.trim().replace(Regex("\\s+"), " ")
        if (keyword.isBlank()) return false
        if (keyword.length > 200) {
            uiState = uiState.copy(errorMessage = "Từ khóa tối đa 200 ký tự")
            return false
        }

        // Nhập tên chủ đề có sẵn thì chọn luôn chủ đề đó.
        val value = topics.firstOrNull { it.equals(keyword, ignoreCase = true) } ?: keyword
        val alreadySelected = uiState.selectedTopics.any { it.equals(value, ignoreCase = true) }
        uiState = uiState.copy(
            selectedTopics = if (alreadySelected) uiState.selectedTopics else uiState.selectedTopics + value,
            errorMessage = "",
            isSaved = false
        )
        return true
    }

    private fun setLoading(isLoading: Boolean) {
        uiState = uiState.copy(
            isLoading = isLoading,
            errorMessage = "",
            isSaved = false
        )
    }

    private fun setError(
        message: String,
        needsLogin: Boolean = false
    ) {
        uiState = uiState.copy(
            isLoading = false,
            errorMessage = message,
            isSaved = false,
            needsLogin = needsLogin
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
                    setError(
                        message = "Bạn cần đăng nhập lại",
                        needsLogin = true
                    )
                    return@launch
                }

                val response = RetrofitClient.apiService
                    .getPreferences("Bearer $token")

                val data = response.body()

                if (response.isSuccessful && data != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        selectedTopics = data.preferences.map { it.topic },
                        isSaved = false,
                        needsLogin = false
                    )
                } else if (response.code() == 401) {
                    setError(
                        message = "Phiên không còn hợp lệ, hãy đăng nhập lại",
                        needsLogin = true
                    )
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
                    setError(
                        message = "Bạn cần đăng nhập lại",
                        needsLogin = true
                    )
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
                            isSaved = true,
                            needsLogin = false
                        )
                    }

                    response.code() == 401 -> {
                        setError(
                            message = "Phiên không còn hợp lệ, hãy đăng nhập lại",
                            needsLogin = true
                        )
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
