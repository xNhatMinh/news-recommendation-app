package com.example.newsrecommendation.ui.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.PreferencesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color

@Composable
fun PreferencesScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
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

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) {
            onLoginClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("Quay lại")
        }

        Text(
            text = "Chủ đề yêu thích",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        for (rowTopics in topics.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (topic in rowTopics) {
                    FilterChip(
                        selected = topic in uiState.selectedTopics,
                        onClick = {
                            viewModel.toggleTopic(topic)
                        },
                        label = {
                            Text(topic)
                        },
                        enabled = !uiState.isLoading
                    )
                }
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