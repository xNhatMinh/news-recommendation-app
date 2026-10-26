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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val email = viewModel.email
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val uiState = viewModel.uiState
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Đăng ký tài khoản",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                viewModel.onEmailChange(it)
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mật khẩu
        OutlinedTextField(
            value = password,
            onValueChange = {
                viewModel.onPasswordChange(it)
            },
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Text(if (passwordVisible) "Ẩn" else "Hiện")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Xác nhận mật khẩu
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                viewModel.onConfirmPasswordChange(it)
            },
            label = { Text("Xác nhận mật khẩu") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    }
                ) {
                    Text(if (confirmPasswordVisible) "Ẩn" else "Hiện")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nút đăng ký
        Button(
            onClick = {
                viewModel.register()
            },
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if(uiState.isLoading){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
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
                color = MaterialTheme.colorScheme.primary
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