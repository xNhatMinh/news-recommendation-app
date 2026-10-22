package com.example.newsrecommendation.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.layout.ContentScale

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    val uriHandler = LocalUriHandler.current

    fun loadNews() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()

        val endDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = dateFormat.format(calendar.time)

        viewModel.loadRecommendations(
            startDate = startDate,
            endDate = endDate
        )
    }

    LaunchedEffect(Unit) {
        loadNews()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tin tức dành cho bạn",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreferencesClick) {
                Text("Chủ đề yêu thích")
            }

            TextButton(onClick = onLogoutClick) {
                Text("Đăng xuất")
            }
        }

        OutlinedButton(
            onClick = {
                loadNews()
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tải lại tin")
        }

        if (uiState.isLoading) {
            Text("Đang tải tin...")
        } else if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        } else if (uiState.articles.isEmpty()) {
            Text("Chưa có tin phù hợp trong khoảng thời gian này")
        }

        LaunchedEffect(uiState.needsLogin) {
            if (uiState.needsLogin) {
                onLogoutClick()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(uiState.articles) { article ->
                Card(
                    onClick = {
                        uriHandler.openUri(article.url)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    val imageUrl = article.image_url

                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = article.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val category = article.category

                        if (!category.isNullOrBlank()) {
                            Text(
                                text = category,
                                color = Color(0xFF2563EB),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = article.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        val publishedAt = article.published_at

                        if (!publishedAt.isNullOrBlank()) {
                            Text(
                                text = "Ngày đăng: ${publishedAt.take(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}