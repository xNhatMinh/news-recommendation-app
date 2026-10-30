package com.example.newsrecommendation.ui.screens.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.newsrecommendation.model.NotificationItem
import com.example.newsrecommendation.ui.viewmodel.NotificationsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val state = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current
    val busy = state.isLoading || state.isLoadingMore || state.isUpdating || state.openingId != null
    val readAllColor = if (!busy && state.unreadCount > 0) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.refreshIfChanged()
                delay(30_000)
            }
        }
    }
    LaunchedEffect(state.needsLogin) {
        if (state.needsLogin) onLoginClick()
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Text("Thông báo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.markAllRead() }, enabled = !busy && state.unreadCount > 0) {
                Box(Modifier.size(28.dp, 24.dp)) {
                    Icon(Icons.Default.Check, contentDescription = "Đánh dấu tất cả đã đọc", modifier = Modifier.size(22.dp).align(Alignment.CenterStart), tint = readAllColor)
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp).align(Alignment.CenterEnd), tint = readAllColor)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.loadNotifications(false) }, enabled = !busy) {
                Text("Tất cả (${state.totalCount})", fontWeight = if (!state.unreadOnly) FontWeight.Bold else FontWeight.Normal)
            }
            Spacer(Modifier.weight(1f))
            FilterChip(
                selected = state.unreadOnly,
                onClick = { viewModel.loadNotifications(!state.unreadOnly) },
                enabled = !busy,
                label = { Text("Chưa đọc ${state.unreadCount}", fontSize = 12.sp) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        if (state.errorMessage.isNotEmpty()) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            TextButton(onClick = { viewModel.loadNotifications() }, enabled = !busy) { Text("Thử lại") }
        }
        if (state.isLoading) {
            Text("Đang tải thông báo...", modifier = Modifier.padding(24.dp))
        } else if (state.notifications.isEmpty() && state.errorMessage.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f).padding(28.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = if (state.unreadOnly) "Bạn đã đọc hết thông báo." else "Chưa có thông báo. Khi có bài mới phù hợp với sở thích, thông báo sẽ xuất hiện ở đây.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(state.notifications, key = { it.id }) { item ->
                    NotificationRow(item, enabled = !busy) {
                        viewModel.openNotification(item, onArticleClick)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                }
                if (state.hasMore) {
                    item {
                        OutlinedButton(
                            onClick = { viewModel.loadMore() }, enabled = !busy,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) { Text(if (state.isLoadingMore) "Đang tải..." else "Xem thêm") }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, enabled = enabled,
        color = if (item.is_read) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                if (!item.image_url.isNullOrBlank()) {
                    AsyncImage(model = item.image_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Có bài viết mới về “${item.topic}”: ${item.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.is_read) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotBlank()) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(notificationTime(item.created_at), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            }
            Box(Modifier.padding(top = 6.dp).size(7.dp)) {
                if (!item.is_read) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
                }
            }
        }
    }
}

private fun notificationTime(value: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(value) ?: return ""
        val minutes = ((System.currentTimeMillis() - date.time) / DateUtils.MINUTE_IN_MILLIS).coerceAtLeast(0)
        when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            minutes < 24 * 60 -> "${minutes / 60} giờ trước"
            minutes < 2 * 24 * 60 -> "Hôm qua"
            minutes < 7 * 24 * 60 -> "${minutes / (24 * 60)} ngày trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
        }
    } catch (_: Exception) {
        value.take(10)
    }
}
