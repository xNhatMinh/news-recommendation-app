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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.FilterChip

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    var showMenu by remember { mutableStateOf(false) }

    val selectedFilter = uiState.selectedFilter
    val filters = listOf("Mới nhất", "Dành cho bạn") + uiState.categories
    val visibleArticles = uiState.articles
    val listState = rememberLazyListState()

    fun openPreferences() {
        viewModel.onPreferencesOpened()
        onPreferencesClick()
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialNews()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.loadNotificationCount()
                delay(30_000)
            }
        }
    }

    LaunchedEffect(filters) {
        if (selectedFilter !in filters) {
            viewModel.loadNews("Mới nhất")
        }
    }

    LaunchedEffect(selectedFilter, uiState.isLoading) {
        if (uiState.isLoading) {
            listState.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Mở menu"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Đăng xuất") },
                        onClick = {
                            showMenu = false
                            onLogoutClick()
                        }
                    )
                }
            }

            Text(
                text = "Bản tin",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { openPreferences() }) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Chủ đề yêu thích"
                )
            }

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = {
                    if (uiState.unreadNotificationCount > 0) {
                        Badge { Text(if (uiState.unreadNotificationCount > 99) "99+" else uiState.unreadNotificationCount.toString()) }
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
                }
            }
        }

        TabRow(
            selectedTabIndex = 0,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = true,
                onClick = {
                    viewModel.loadNews("Mới nhất")
                },
                text = {
                    Text("Tin tức")
                }
            )

            Tab(
                selected = false,
                onClick = onSummaryClick,
                text = {
                    Text("Tổng hợp")
                }
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        if (filter != selectedFilter) {
                            viewModel.loadNews(filter)
                        }
                    },
                    label = {
                        Text(filter)
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Text("Đang tải tin...")
        } else if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        } else if (visibleArticles.isEmpty()) {
            if (selectedFilter == "Dành cho bạn") {
                Text("Chưa có tin gợi ý phù hợp. Bạn có thể chọn lại chủ đề yêu thích.")
                TextButton(onClick = { openPreferences() }) {
                    Text("Chọn chủ đề yêu thích")
                }
            } else {
                Text("Chưa có tin trong mục này")
            }
        }

        LaunchedEffect(uiState.needsLogin) {
            if (uiState.needsLogin) {
                onLogoutClick()
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(visibleArticles) { index, article ->
                val imageUrl = article.image_url
                val featured = index == 0 && !imageUrl.isNullOrBlank()
                val category = article.category.orEmpty().ifBlank { "Tin tức" }
                val date = article.published_at?.take(10).orEmpty()

                Card(
                    onClick = {
                        onArticleClick(article.url)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (featured) 210.dp else 180.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (featured) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "TIN NỔI BẬT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )

                                    Text(
                                        text = article.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "$category · $date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    if (!featured) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = "$category · $date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (article.description.isNotBlank()) {
                                Text(
                                    text = article.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.hasMore) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.loadMore() },
                        enabled = !uiState.isLoadingMore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.isLoadingMore) "Đang tải thêm..." else "Xem thêm tin"
                        )
                    }
                }
            }
        }
    }
}
