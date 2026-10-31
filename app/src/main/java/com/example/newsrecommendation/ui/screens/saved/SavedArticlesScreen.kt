package com.example.newsrecommendation.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.newsrecommendation.R
import com.example.newsrecommendation.ui.viewmodel.SavedArticlesViewModel
import kotlinx.coroutines.awaitCancellation

@Composable
fun SavedArticlesScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    viewModel: SavedArticlesViewModel = viewModel()
) {
    val state = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Tải lại khi quay về, để bài vừa bỏ lưu biến mất khỏi danh sách.
            viewModel.loadArticles()
            awaitCancellation()
        }
    }
    LaunchedEffect(state.needsLogin) {
        if (state.needsLogin) onLoginClick()
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Text("Bài đã lưu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (state.errorMessage.isNotEmpty()) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { viewModel.retry() }) {
                Text("Thử lại")
            }
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.articles.isEmpty() && state.errorMessage.isEmpty() -> Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_bookmark_outline),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Bạn chưa lưu bài báo nào", fontWeight = FontWeight.Bold)
                Text("Bấm biểu tượng lưu khi đọc bài để xem lại tại đây.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!state.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.articles, key = { it.url }) { article ->
                    Card(
                        onClick = { onArticleClick(article.url) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!article.image_url.isNullOrBlank()) {
                                AsyncImage(
                                    model = article.image_url,
                                    contentDescription = null,
                                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    listOfNotNull(article.category, article.published_at?.take(10)).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(article.title, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                if (article.description.isNotBlank()) {
                                    Text(
                                        article.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                if (state.hasMore) {
                    item {
                        OutlinedButton(
                            onClick = { viewModel.loadMore() },
                            enabled = !state.isLoadingMore,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.isLoadingMore) "Đang tải thêm..." else "Xem thêm bài đã lưu")
                        }
                    }
                }
            }
        }
    }
}
