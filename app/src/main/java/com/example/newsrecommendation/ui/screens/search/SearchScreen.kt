package com.example.newsrecommendation.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.newsrecommendation.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state = viewModel.uiState
    val keyboard = LocalSoftwareKeyboardController.current

    fun submitSearch() {
        keyboard?.hide()
        viewModel.search()
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Text("Tìm kiếm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm bài báo, ví dụ: giá vàng") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa từ khóa")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submitSearch() })
        )
        Button(
            onClick = { submitSearch() },
            enabled = state.query.isNotBlank() && !state.isLoading && !state.isLoadingMore,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(if (state.isLoading) "Đang tìm..." else "Tìm kiếm")
        }

        if (state.errorMessage.isNotEmpty()) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { if (state.articles.isEmpty()) viewModel.search() else viewModel.loadMore() }) {
                Text("Thử lại")
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.hasSearched -> Text(
                "Nhập từ khóa để tìm trong các bài báo đã lưu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            state.articles.isEmpty() && state.errorMessage.isEmpty() -> Text(
                "Không tìm thấy bài phù hợp. Bạn thử từ khóa khác nhé.",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

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
                            Text(
                                article.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
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
                        Text(if (state.isLoadingMore) "Đang tải thêm..." else "Xem thêm kết quả")
                    }
                }
            }
        }
    }
}
