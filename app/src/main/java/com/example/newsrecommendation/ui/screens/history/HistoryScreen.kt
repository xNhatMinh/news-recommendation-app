package com.example.newsrecommendation.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) {
            onLoginClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("Quay lại")
        }

        Text(
            text = "Lịch sử gợi ý",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedButton(
            onClick = {
                viewModel.loadHistory()
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tải lại lịch sử")
        }

        when {
            uiState.isLoading -> {
                Text("Đang tải lịch sử...")
            }

            uiState.errorMessage.isNotEmpty() -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.history.isEmpty() -> {
                Text("Bạn chưa có lịch sử gợi ý")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.history,
                        key = { it.id }
                    ) { item ->
                        OutlinedCard(
                            onClick = {
                                uriHandler.openUri(item.url)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Được gợi ý: ${
                                        item.created_at.replace('T', ' ').take(16)
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Đọc bài gốc →",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}