package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private var deviceJob: Job? = null

    var isLoggedIn by mutableStateOf(false)
        private set

    private val restoreJob = viewModelScope.launch {
        var hasSavedToken = false
        try {
            val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
            if (token.isNullOrBlank()) return@launch
            hasSavedToken = true
            val response = RetrofitClient.apiService.getCurrentUser("Bearer $token")
            if (response.code() == 401) {
                withContext(Dispatchers.IO) { tokenManager.clearToken() }
                isLoggedIn = false
            } else {
                isLoggedIn = true
                if (response.isSuccessful) registerDevice()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Mất mạng không đồng nghĩa với hết phiên; API cá nhân sẽ kiểm tra lại.
            isLoggedIn = hasSavedToken
        }
    }

    suspend fun awaitRestored() {
        restoreJob.join()
    }

    fun onLoggedIn() {
        restoreJob.cancel()
        isLoggedIn = true
        registerDevice()
    }

    suspend fun clearSession() {
        restoreJob.cancelAndJoin()
        deviceJob?.cancelAndJoin()
        withContext(Dispatchers.IO) { tokenManager.clearToken() }
        isLoggedIn = false
        try {
            CredentialManager.create(getApplication<Application>())
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Phiên của app đã được xóa; bỏ qua lỗi từ nhà cung cấp tài khoản.
        }
    }

    private fun registerDevice() {
        deviceJob?.cancel()
        deviceJob = viewModelScope.launch {
            val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
            if (!token.isNullOrBlank()) registerDeviceToken(token)
        }
    }
}
