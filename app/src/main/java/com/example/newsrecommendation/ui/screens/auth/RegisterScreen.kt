package com.example.newsrecommendation.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
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
            value = viewModel.email,
            onValueChange = {
                viewModel.onEmailChange(it)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mật khẩu
        TextField(
            value = viewModel.password,
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
            value = viewModel.confirmPassword,
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
                if(viewModel.validateRegister()){
                    viewModel.setLoading(true)
//                    viewModel.setSuccess("Dữ liệu hợp lệ")
                }
            },
            enabled = !viewModel.uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(viewModel.uiState.isLoading){
                CircularProgressIndicator()
            } else {
                Text("Đăng ký")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Thông báo lỗi
        if (viewModel.uiState.errorMessage.isNotEmpty()) {
            Text(
                text = viewModel.uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
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