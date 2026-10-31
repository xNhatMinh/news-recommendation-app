package com.example.newsrecommendation.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.RegisterRequest
import kotlinx.coroutines.*

class RegisterViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onEmailChange(newEmail: String){
        email = newEmail
    }

    fun onPasswordChange(newPassword: String){
        password = newPassword
    }

    fun onConfirmPasswordChange(newPassword: String) {
        confirmPassword = newPassword
    }

    fun validateRegister(): Boolean {
        val error = when {
            email.isBlank() ->
                "Bạn chưa nhập email"

            password.isBlank() ->
                "Bạn chưa nhập mật khẩu"

            password.length < 8 ->
                "Mật khẩu phải có ít nhất 8 ký tự"

            confirmPassword.isBlank() ->
                "Bạn chưa xác nhận mật khẩu"

            password != confirmPassword ->
                "Mật khẩu xác nhận không khớp"

            else -> ""
        }

        uiState = uiState.copy(
            errorMessage = error
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

    fun register() {
        if(uiState.isLoading || !validateRegister()) {
            return
        }

        val request = RegisterRequest(
            email = email.trim(),
            password = password
        )

        setLoading(true)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(request)

                if(response.isSuccessful){
                    setSuccess("Đăng ký thành công")
                } else {
                    val message = when(response.code()){
                        409 -> "Email đã được đăng ký"
                        422 -> "Email hoặc mật khẩu chưa hợp lệ"
                        429 -> "Bạn thử đăng ký quá nhiều lần, vui lòng thử lại sau"
                        503 -> "Dịch vụ đăng ký tạm thời chưa khả dụng, vui lòng thử lại"
                        else -> "Đăng ký thất bại, vui lòng thử lại"
                    }
                    setError(message)
                }
            } catch (e: CancellationException){
                throw e
            } catch (e: Exception) {
                setError("Không thể hoàn tất đăng ký, vui lòng thử lại")
            }
        }
    }
}