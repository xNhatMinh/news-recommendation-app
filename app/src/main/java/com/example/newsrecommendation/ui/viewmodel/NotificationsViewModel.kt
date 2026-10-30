package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.NotificationItem
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.apiService
    private var nextOffset = 0
    private var hasLoaded = false
    private var loadJob: Job? = null
    private var checkingCounts = false

    var uiState by mutableStateOf(NotificationsUiState())
        private set

    private suspend fun authorization(): String? {
        val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
        if (token.isNullOrBlank()) {
            fail(401)
            return null
        }
        return "Bearer $token"
    }

    private fun fail(code: Int? = null) {
        uiState = uiState.copy(
            isLoading = false, isLoadingMore = false, isUpdating = false, openingId = null,
            errorMessage = if (code == 401) "Bạn cần đăng nhập lại" else "Không thể tải thông báo, hãy thử lại",
            needsLogin = code == 401
        )
    }

    fun loadNotifications(unreadOnly: Boolean = uiState.unreadOnly) {
        fetch(unreadOnly, append = false)
    }

    fun loadMore() {
        if (uiState.isLoading || uiState.isLoadingMore || uiState.isUpdating || uiState.openingId != null || !uiState.hasMore) return
        fetch(uiState.unreadOnly, append = true)
    }

    private fun fetch(unreadOnly: Boolean, append: Boolean) {
        if (uiState.isUpdating || uiState.openingId != null) return
        loadJob?.cancel()
        if (!append) nextOffset = 0
        uiState = uiState.copy(
            unreadOnly = unreadOnly, isLoading = !append, isLoadingMore = append,
            notifications = if (append) uiState.notifications else emptyList(),
            hasMore = if (append) uiState.hasMore else false, errorMessage = ""
        )
        loadJob = viewModelScope.launch {
            try {
                val auth = authorization() ?: return@launch
                val response = api.getNotifications(auth, offset = nextOffset, unreadOnly = unreadOnly)
                val data = response.body()
                if (!response.isSuccessful || data == null) {
                    fail(response.code())
                    return@launch
                }
                nextOffset += data.results.size
                val rows = if (append) uiState.notifications + data.results else data.results
                uiState = uiState.copy(
                    notifications = rows.distinctBy { it.id },
                    totalCount = data.total_count, unreadCount = data.unread_count,
                    hasMore = data.has_more, isLoading = false, isLoadingMore = false
                )
                hasLoaded = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail()
            }
        }
    }

    fun refreshIfChanged() {
        if (uiState.isLoading || uiState.isLoadingMore || uiState.isUpdating || uiState.openingId != null || checkingCounts) return
        if (!hasLoaded) {
            loadNotifications()
            return
        }
        checkingCounts = true
        viewModelScope.launch {
            try {
                val auth = authorization() ?: return@launch
                val response = api.getNotificationCounts(auth)
                val data = response.body()
                if (response.code() == 401) fail(401)
                else if (response.isSuccessful && data != null &&
                    (data.total_count != uiState.totalCount || data.unread_count != uiState.unreadCount)) {
                    loadNotifications()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Thử lại ở lần kiểm tra tiếp theo.
            } finally {
                checkingCounts = false
            }
        }
    }

    fun openNotification(item: NotificationItem, onOpen: (String) -> Unit) {
        if (uiState.isUpdating || uiState.openingId != null || uiState.isLoading || uiState.isLoadingMore) return
        if (item.is_read) {
            onOpen(item.url)
            return
        }
        uiState = uiState.copy(openingId = item.id, errorMessage = "")
        viewModelScope.launch {
            try {
                val auth = authorization() ?: return@launch
                val response = api.readNotification(auth, item.id)
                val data = response.body()
                if (!response.isSuccessful || data == null) {
                    fail(response.code())
                    return@launch
                }
                if (data.total_count != uiState.totalCount) hasLoaded = false
                val rows = if (uiState.unreadOnly) {
                    nextOffset = (nextOffset - 1).coerceAtLeast(0)
                    uiState.notifications.filterNot { it.id == item.id }
                } else uiState.notifications.map { if (it.id == item.id) it.copy(is_read = true) else it }
                uiState = uiState.copy(
                    notifications = rows, unreadCount = data.unread_count,
                    totalCount = data.total_count, openingId = null,
                    hasMore = uiState.hasMore && (!uiState.unreadOnly || data.unread_count > 0)
                )
                onOpen(item.url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail()
            }
        }
    }

    fun markAllRead() {
        if (uiState.unreadCount == 0 || uiState.isUpdating || uiState.openingId != null || uiState.isLoading || uiState.isLoadingMore) return
        uiState = uiState.copy(isUpdating = true, errorMessage = "")
        viewModelScope.launch {
            try {
                val auth = authorization() ?: return@launch
                val response = api.readAllNotifications(auth)
                val data = response.body()
                if (!response.isSuccessful || data == null) {
                    fail(response.code())
                    return@launch
                }
                val countsChanged = data.total_count != uiState.totalCount
                val unreadOnly = uiState.unreadOnly
                if (unreadOnly) nextOffset = 0
                uiState = uiState.copy(
                    notifications = if (unreadOnly) emptyList() else uiState.notifications.map { it.copy(is_read = true) },
                    unreadCount = data.unread_count, totalCount = data.total_count,
                    isUpdating = false, hasMore = !unreadOnly && uiState.hasMore
                )
                if (countsChanged || (unreadOnly && data.unread_count > 0)) loadNotifications()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail()
            }
        }
    }
}
