package com.example.newsrecommendation.ui.screens.preferences

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.model.NewsTopics
import com.example.newsrecommendation.ui.viewmodel.PreferencesViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: PreferencesViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val topics = NewsTopics.all
    val selectedTopics = uiState.selectedTopics.filter { it in topics }.distinct()
    val keywords = uiState.selectedTopics.filterNot { it in topics }.distinct()
    var showAllTopics by rememberSaveable { mutableStateOf(false) }
    var keywordText by rememberSaveable { mutableStateOf("") }
    val visibleTopics = if (showAllTopics) topics else topics.take(9)

    fun addKeyword() {
        if (viewModel.addKeyword(keywordText)) keywordText = ""
    }

    LaunchedEffect(Unit) { viewModel.loadPreferences() }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBackClick()
    }
    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) onLoginClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cá nhân hóa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tùy chỉnh bản tin của bạn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.Close, contentDescription = "Đóng")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Chủ đề quan tâm (${selectedTopics.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                for (topic in visibleTopics) {
                    TopicChip(
                        label = topic,
                        selected = topic in selectedTopics,
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.toggleTopic(topic) }
                    )
                }
            }
            TextButton(
                onClick = { showAllTopics = !showAllTopics },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (showAllTopics) "Thu gọn ▴" else "Xem thêm ${topics.size - 9} chủ đề ▾",
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã chọn",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.clearTopics() },
                    enabled = selectedTopics.isNotEmpty() && !uiState.isLoading
                ) {
                    Text("Xóa tất cả", fontSize = 12.sp)
                }
            }
            if (selectedTopics.isEmpty()) {
                Text(
                    text = "Bạn chưa chọn chủ đề nào.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedTopics, key = { it }) { topic ->
                        TopicChip(
                            label = topic,
                            selected = true,
                            enabled = !uiState.isLoading,
                            onClick = { viewModel.toggleTopic(topic) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Theo dõi từ khóa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ưu tiên các tin liên quan đến từ khóa bạn chọn.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            OutlinedTextField(
                value = keywordText,
                onValueChange = { keywordText = it },
                placeholder = { Text("Thêm từ khóa", fontSize = 14.sp) },
                singleLine = true,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { addKeyword() },
                        enabled = keywordText.isNotBlank() && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm từ khóa")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (keywords.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (keyword in keywords) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.toggleTopic(keyword) },
                            enabled = !uiState.isLoading,
                            label = {
                                Text(keyword, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Bỏ từ khóa $keyword",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Button(
                onClick = {
                    // Lưu cả từ khóa đang nhập nếu chưa bấm dấu cộng.
                    if (keywordText.isBlank() || viewModel.addKeyword(keywordText)) {
                        keywordText = ""
                        viewModel.savePreferences()
                    }
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (uiState.isLoading) "Đang xử lý..." else "Tiếp tục",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TopicChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    )
}
