package com.example.newsrecommendation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.LoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun validateLogin(): Boolean {
        val error = when {
            email.isBlank() ->
                "Bạn chưa nhập email"

            password.isBlank() ->
                "Bạn chưa nhập mật khẩu"

            password.length < 8 ->
                "Mật khẩu phải có ít nhất 8 ký tự"

            else -> ""
        }

        uiState = uiState.copy(
            errorMessage = error,
            successMessage = ""
        )

        return error.isEmpty()
    }

    fun setLoading(isLoading: Boolean) {
        uiState = uiState.copy(
            isLoading = isLoading,
            errorMessage = "",
            successMessage = ""
        )
    }

    fun setSuccess(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            successMessage = message,
            errorMessage = "",
            isLoggedIn = true
        )
    }

    fun setError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            successMessage = "",
            errorMessage = message,
            isLoggedIn = false
        )
    }

    fun login() {
        if (uiState.isLoading || !validateLogin()) {
            return
        }

        val request = LoginRequest(
            email = email.trim(),
            password = password
        )

        setLoading(true)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful) {
                    val token = response.body()?.access_token

                    if(token.isNullOrBlank()) {
                        setError("Backend không trả về token hợp lệ")
                    } else {
                        // đa luồng
                        // chuyển việc ghi file sang luồng dành cho đọc/ghi dữ liệu
                        // giúp giao diện không bị chặn.
                        withContext(Dispatchers.IO) {
                            tokenManager.saveToken(token)
                        }

                        setSuccess("Đăng nhập thành công")
                    }
                } else {
                    val message = when (response.code()) {
                        401 -> "Email hoặc mật khẩu không đúng"
                        422 -> "Thông tin đăng nhập không hợp lệ"
                        else -> "Đăng nhập thất bại, vui lòng thử lại"
                    }
                    setError(message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể hoàn tất đăng nhập, vui lòng thử lại")
            }
        }
    }

    fun checkSavedLogin() {
        if (uiState.isLoading) return
        setLoading(true)

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    tokenManager.getToken()
                }

                if (token.isNullOrBlank()) {
                    setLoading(false)
                    return@launch
                }

                val response = RetrofitClient.apiService
                    .getCurrentUser("Bearer $token")

                when (response.code()) {
                    200 -> setSuccess("Bạn đã đăng nhập")

                    401 -> {
                        withContext(Dispatchers.IO) {
                            tokenManager.clearToken()
                        }
                        setLoading(false)
                    }

                    else -> setError("Chưa kiểm tra được phiên đăng nhập")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể kiểm tra phiên đăng nhập")
            }
        }
    }
}