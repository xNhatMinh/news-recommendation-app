package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val pageSize = 20
    private var nextOffset = 0
    private var searchJob: Job? = null

    var uiState by mutableStateOf(SearchUiState())
        private set

    fun updateQuery(query: String) {
        if (query == uiState.query) return
        searchJob?.cancel()
        nextOffset = 0
        uiState = SearchUiState(query = query.take(200))
    }

    fun search() {
        val query = uiState.query.trim().replace(Regex("\\s+"), " ")
        if (query.isBlank()) return
        fetchArticles(query, append = false)
    }

    fun loadMore() {
        if (uiState.isLoading || uiState.isLoadingMore || !uiState.hasMore) return
        fetchArticles(uiState.query, append = true)
    }

    private fun showError(message: String) {
        uiState = uiState.copy(
            isLoading = false,
            isLoadingMore = false,
            errorMessage = message
        )
    }

    private fun fetchArticles(query: String, append: Boolean) {
        searchJob?.cancel()
        val offset = if (append) nextOffset else 0
        uiState = if (append) {
            uiState.copy(isLoadingMore = true, errorMessage = "")
        } else {
            nextOffset = 0
            SearchUiState(query = query, hasSearched = true, isLoading = true)
        }

        searchJob = viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchArticles(
                    query = query,
                    limit = pageSize,
                    offset = offset
                )
                val data = response.body()
                if (response.isSuccessful && data != null) {
                    nextOffset = offset + data.results.size
                    val articles = if (append) uiState.articles + data.results else data.results
                    uiState = uiState.copy(
                        articles = articles.distinctBy { it.url },
                        hasMore = data.has_more,
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = ""
                    )
                } else {
                    showError("Không thể tìm kiếm, vui lòng thử lại")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showError("Không thể kết nối máy chủ, vui lòng thử lại")
            }
        }
    }
}
