package com.example.newsrecommendation.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

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
}