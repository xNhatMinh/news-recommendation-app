package com.example.newsrecommendation.ui.screens.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.SummaryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.unit.sp

@Composable
fun SummaryScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SummaryViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val uriHandler = LocalUriHandler.current

    val topics = listOf(
        "Thời sự",
        "Thế giới",
        "Kinh doanh",
        "Khoa học",
        "Thể thao",
        "Giải trí",
        "Sức khỏe"
    )

    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) {
            onLoginClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("Quay lại")
        }

        Text(
            text = "Tổng hợp tin",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Chọn chủ đề để tổng hợp tin trong 30 ngày gần đây")

        for (rowTopics in topics.chunked(2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (topic in rowTopics) {
                    FilterChip(
                        selected = topic == uiState.selectedTopic,
                        onClick = {
                            viewModel.selectTopic(topic)
                        },
                        label = {
                            Text(topic)
                        },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }

        Button(
            onClick = {
                val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )
                val calendar = Calendar.getInstance()

                val endDate = dateFormat.format(calendar.time)

                calendar.add(Calendar.DAY_OF_YEAR, -29)
                val startDate = dateFormat.format(calendar.time)

                viewModel.createSummary(
                    startDate = startDate,
                    endDate = endDate
                )
            },
            enabled = !uiState.isLoading &&
                    uiState.selectedTopic.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.isLoading) {
                    "Đang tổng hợp..."
                } else {
                    "Tổng hợp tin"
                }
            )
        }

        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (uiState.summary.isNotBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = uiState.selectedTopic,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Tổng hợp từ ${uiState.sources.size} bài báo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val displaySummary = uiState.summary
                .replace("**", "")
                .replace(Regex("""(?m)^#{1,6}[ \t]*"""), "")
                .replace(Regex("""(?m)^[ \t]*[-*][ \t]+"""), "• ")
                .trim()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = displaySummary,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 26.sp
                )
            }

            Text(
                text = "Bài báo nguồn",
                style = MaterialTheme.typography.titleMedium
            )

            for (source in uiState.sources) {
                OutlinedCard(
                    onClick = {
                        uriHandler.openUri(source.url)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = source.title,
                            style = MaterialTheme.typography.titleSmall
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