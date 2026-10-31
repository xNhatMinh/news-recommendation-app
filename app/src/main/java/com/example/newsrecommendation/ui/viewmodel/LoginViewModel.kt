package com.example.newsrecommendation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.LoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import android.app.Activity
import android.app.Application
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.newsrecommendation.auth.googleIdToken
import com.example.newsrecommendation.model.GoogleLoginRequest
import androidx.lifecycle.AndroidViewModel
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.example.newsrecommendation.model.DeviceTokenRequest
import java.util.concurrent.TimeUnit
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
            isGoogleLoading = false,
            errorMessage = "",
            successMessage = ""
        )
    }

    fun setSuccess(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            isGoogleLoading = false,
            successMessage = message,
            errorMessage = "",
            isLoggedIn = true
        )
    }

    fun setError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            isGoogleLoading = false,
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
                        403 -> "Tài khoản đã bị vô hiệu hóa"
                        409 -> "Email đã được liên kết với một tài khoản khác."
                        422 -> "Thông tin đăng nhập không hợp lệ"
                        429 -> "Bạn thử đăng nhập quá nhiều lần, vui lòng thử lại sau"
                        503 -> "Dịch vụ đăng nhập tạm thời chưa khả dụng, vui lòng thử lại"
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


    fun loginWithGoogle(activity: Activity) {
        if (uiState.isLoading) return
        setLoading(true)
        uiState = uiState.copy(isGoogleLoading = true)
        viewModelScope.launch {
            try {
                val googleToken = googleIdToken(activity)
                val response = RetrofitClient.apiService.loginGoogle(GoogleLoginRequest(googleToken))
                val token = response.body()?.access_token
                if (response.isSuccessful && !token.isNullOrBlank()) {
                    withContext(Dispatchers.IO) { tokenManager.saveToken(token) }
                    setSuccess("Đăng nhập thành công")
                } else {
                    setError(when (response.code()) {
                        401 -> "Không xác thực được tài khoản Google, vui lòng thử lại"
                        403 -> "Tài khoản đã bị vô hiệu hóa"
                        409 -> "Email này đã có tài khoản. Hãy đăng nhập bằng email/mật khẩu trước."
                        503 -> "Dịch vụ đăng nhập tạm thời chưa khả dụng, vui lòng thử lại"
                        else -> "Không thể đăng nhập Google, vui lòng thử lại"
                    })
                }
            } catch (_: GetCredentialCancellationException) {
                setLoading(false)
            } catch (e: CancellationException) {
                throw e
            } catch (_: NoCredentialException) {
                setError("Chưa tìm thấy tài khoản Google khả dụng trên thiết bị")
            } catch (_: GetCredentialException) {
                setError("Không mở được đăng nhập Google, vui lòng thử lại")
            } catch (_: Exception) {
                setError("Không thể đăng nhập Google, vui lòng thử lại")
            }
        }
    }

}

internal suspend fun registerDeviceToken(accessToken: String) {
    try {
        val fcmToken = withContext(Dispatchers.IO) {
            Tasks.await(
                FirebaseMessaging.getInstance().token,
                10,
                TimeUnit.SECONDS
            )
        }

        val response = RetrofitClient.apiService.saveDeviceToken(
            authorization = "Bearer $accessToken",
            request = DeviceTokenRequest(fcm_token = fcmToken)
        )

        if (response.isSuccessful) {
            Log.d("FCM_DEVICE", "Đã đăng ký thiết bị")
        } else {
            Log.w("FCM_DEVICE", "Lưu token thất bại: ${response.code()}")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("FCM_DEVICE", "Không đăng ký được thiết bị", e)
    }
}