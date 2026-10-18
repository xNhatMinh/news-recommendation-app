package com.example.newsrecommendation.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.RegisterViewModel
import androidx.compose.ui.graphics.Color

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val email = viewModel.email
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Đăng ký tài khoản")

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        TextField(
            value = email,
            onValueChange = {
                viewModel.onEmailChange(it)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mật khẩu
        TextField(
            value = password,
            onValueChange = {
                viewModel.onPasswordChange(it)
            },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Xác nhận mật khẩu
        TextField(
            value = confirmPassword,
            onValueChange = {
                viewModel.onConfirmPasswordChange(it)
            },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nút đăng ký
        Button(
            onClick = {
                viewModel.register()
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(uiState.isLoading){
                CircularProgressIndicator()
            } else {
                Text("Đăng ký")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Thông báo lỗi
        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Thông báo đăng ký thành công
        if (uiState.successMessage.isNotEmpty()) {
            Text(
                text = uiState.successMessage,
                color = Color(0xFF2E7D32)
            )
        }

        // Quay lại đăng nhập
        TextButton(
            onClick = onLoginClick
        ) {
            Text("Đã có tài khoản? Đăng nhập")
        }
    }
}