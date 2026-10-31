package com.example.newsrecommendation.ui.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.PasswordResetRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf("")
        private set
    var successMessage by mutableStateOf("")
        private set

    fun prepare(initialEmail: String) {
        email = initialEmail.trim()
        errorMessage = ""
        successMessage = ""
    }

    fun onEmailChange(value: String) {
        email = value
        errorMessage = ""
    }

    fun sendResetEmail() {
        if (isLoading || successMessage.isNotEmpty()) return
        val address = email.trim()
        errorMessage = when {
            address.isEmpty() -> "Bạn chưa nhập email"
            address.length > 254 || !Patterns.EMAIL_ADDRESS.matcher(address).matches() ->
                "Email không hợp lệ"
            else -> ""
        }
        if (errorMessage.isNotEmpty()) return

        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.forgotPassword(PasswordResetRequest(address))
                if (response.isSuccessful) {
                    successMessage = "Nếu email đã được đăng ký, bạn sẽ nhận được liên kết đặt lại mật khẩu. Hãy kiểm tra cả thư rác."
                } else {
                    errorMessage = when (response.code()) {
                        422 -> "Email không hợp lệ"
                        429 -> "Bạn gửi yêu cầu quá nhiều lần, vui lòng thử lại sau"
                        else -> "Chưa gửi được email, vui lòng thử lại sau"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorMessage = "Không kết nối được máy chủ, vui lòng thử lại"
            } finally {
                isLoading = false
            }
        }
    }
}
