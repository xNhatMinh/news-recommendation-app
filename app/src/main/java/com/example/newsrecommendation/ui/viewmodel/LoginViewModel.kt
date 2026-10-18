package com.example.newsrecommendation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.LoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

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
            errorMessage = "",
            successMessage = message
        )
    }

    fun setError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            errorMessage = message,
            successMessage = ""
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
                    setSuccess("Đăng nhập thành công")
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
}