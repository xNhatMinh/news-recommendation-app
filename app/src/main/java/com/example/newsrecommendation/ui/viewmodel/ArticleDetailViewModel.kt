package com.example.newsrecommendation.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.BookmarkRequest
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private var bookmarkJob: Job? = null

    var uiState by mutableStateOf(ArticleDetailUiState())
        private set


    private var explanationJob: Job? = null
    private val explanationCache = mutableMapOf<Pair<String, String>, String>()

    fun dismissExplanation() {
        explanationJob?.cancel()
        uiState = uiState.copy(selectedTerm = "", termExplanation = "", termError = "", isExplainingTerm = false)
    }

    fun explainTerm(term: String) {
        val article = uiState.article ?: return
        explanationJob?.cancel()
        val cached = explanationCache[article.url to term]
        uiState = uiState.copy(selectedTerm = term, termExplanation = cached.orEmpty(), termError = "", isExplainingTerm = cached == null)
        if (cached != null) return
        explanationJob = viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.explainTerm(
                    com.example.newsrecommendation.model.TermExplanationRequest(article.url, term))
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    explanationCache[article.url to term] = result.explanation
                    uiState = uiState.copy(termExplanation = result.explanation)
                } else {
                    val message = when (response.code()) {
                        400, 422 -> "Hãy chọn một từ hoặc cụm từ ngắn có trong bài báo."
                        404 -> "Không tìm thấy bài báo."
                        503 -> "Dịch vụ giải thích AI chưa được cấu hình."
                        else -> "Chưa thể giải thích. Hãy thử lại."
                    }
                    uiState = uiState.copy(termError = message)
                }
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                uiState = uiState.copy(termError = "Không thể kết nối. Hãy thử lại.")
            } finally {
                // A cancelled request must not overwrite the next selection's state.
                if (kotlinx.coroutines.currentCoroutineContext().isActive) {
                    uiState = uiState.copy(isExplainingTerm = false)
                }
            }
        }
    }

    private fun setError(message: String) {
        uiState = ArticleDetailUiState(errorMessage = message)
    }

    fun loadArticle(url: String) {
        if (uiState.isLoading) return

        dismissExplanation()
        explanationCache.clear()
        uiState = ArticleDetailUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getArticleDetail(url = url)

                val article = response.body()

                when {
                    response.isSuccessful && article != null -> {
                        uiState = ArticleDetailUiState(
                            article = article
                        )
                    }

                    response.code() == 404 -> {
                        setError("Không tìm thấy bài báo")
                    }

                    else -> {
                        setError("Không thể tải nội dung bài báo")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Không thể kết nối, vui lòng thử lại")
            }
        }
    }

    fun clearBookmarkState() {
        bookmarkJob?.cancel()
        uiState = uiState.copy(
            isSaved = null, isCheckingBookmark = false, isUpdatingBookmark = false,
            bookmarkError = "", needsLogin = false, resumeBookmark = false
        )
    }

    fun loadBookmarkStatus() {
        requestBookmark(change = false)
    }

    fun toggleBookmark() {
        if (uiState.isSaved == null) return
        requestBookmark(change = true)
    }

    private fun requestBookmark(change: Boolean) {
        val article = uiState.article ?: return
        if (uiState.isCheckingBookmark || uiState.isUpdatingBookmark) return
        val wasSaved = uiState.isSaved == true
        uiState = uiState.copy(
            isCheckingBookmark = !change,
            isUpdatingBookmark = change,
            bookmarkError = ""
        )
        bookmarkJob = viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { tokenManager.getToken() }
                if (token.isNullOrBlank()) {
                    uiState = uiState.copy(needsLogin = true, resumeBookmark = change)
                    return@launch
                }
                val api = RetrofitClient.apiService
                val response = when {
                    !change -> api.getBookmarkStatus("Bearer $token", article.url)
                    wasSaved -> api.removeBookmark("Bearer $token", article.url)
                    else -> api.saveBookmark("Bearer $token", BookmarkRequest(article.url))
                }
                val data = response.body()
                if (response.isSuccessful && data != null) {
                    uiState = uiState.copy(isSaved = data.is_saved)
                } else if (response.code() == 401) {
                    uiState = uiState.copy(needsLogin = true, resumeBookmark = change)
                } else {
                    uiState = uiState.copy(bookmarkError = "Không thể cập nhật trạng thái lưu bài. Hãy thử lại.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                uiState = uiState.copy(bookmarkError = "Không thể kết nối để lưu bài. Hãy thử lại.")
            } finally {
                uiState = uiState.copy(isCheckingBookmark = false, isUpdatingBookmark = false)
            }
        }
    }
}
