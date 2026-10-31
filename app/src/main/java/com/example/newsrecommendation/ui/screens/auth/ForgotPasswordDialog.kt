package com.example.newsrecommendation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    model: ForgotPasswordViewModel = viewModel()
) {
    LaunchedEffect(Unit) { model.prepare(initialEmail) }
    val sent = model.successMessage.isNotEmpty()

    AlertDialog(
        onDismissRequest = { if (!model.isLoading) onDismiss() },
        title = { Text("Quên mật khẩu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nhập email để nhận liên kết đặt lại mật khẩu.")
                OutlinedTextField(
                    value = model.email,
                    onValueChange = model::onEmailChange,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !model.isLoading && !sent,
                    isError = model.errorMessage.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (model.errorMessage.isNotEmpty()) {
                    Text(model.errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (sent) {
                    Text(model.successMessage, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            if (!sent) {
                TextButton(onClick = model::sendResetEmail, enabled = !model.isLoading) {
                    if (model.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (model.isLoading) "Đang gửi..." else "Gửi liên kết")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        },
        dismissButton = {
            if (!sent) {
                TextButton(onClick = onDismiss, enabled = !model.isLoading) { Text("Hủy") }
            }
        }
    )
}
