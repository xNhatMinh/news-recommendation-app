package com.example.newsrecommendation.ui.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.PreferencesViewModel
import androidx.compose.ui.Alignment

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    val topics = listOf(
        "Thời sự",
        "Thế giới",
        "Kinh doanh",
        "Khoa học",
        "Thể thao",
        "Giải trí",
        "Sức khỏe"
    )

    LaunchedEffect(Unit) {
        viewModel.loadPreferences()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Chủ đề yêu thích",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        for (topic in topics) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = topic in uiState.selectedTopics,
                    onCheckedChange = {
                        viewModel.toggleTopic(topic)
                    },
                    enabled = !uiState.isLoading
                )

                Text(text = topic)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.savePreferences()
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                Text("Đang xử lý...")
            } else {
                Text("Lưu chủ đề")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (uiState.isSaved) {
            Text("Đã lưu chủ đề yêu thích")
        }
    }
}