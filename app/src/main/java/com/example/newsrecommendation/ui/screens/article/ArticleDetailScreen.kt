package com.example.newsrecommendation.ui.screens.article

import android.net.Uri
import android.text.TextUtils.htmlEncode
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.newsrecommendation.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsrecommendation.model.ArticleDetail
import com.example.newsrecommendation.ui.viewmodel.ArticleDetailViewModel

@Composable
fun ArticleDetailScreen(
    url: String,
    onBackClick: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onSessionExpired: (Boolean) -> Unit,
    saveAfterLogin: Boolean,
    onSaveHandled: () -> Unit,
    viewModel: ArticleDetailViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(url) {
        viewModel.loadArticle(url)
    }

    LaunchedEffect(isLoggedIn, uiState.article?.url) {
        if (isLoggedIn && uiState.article != null) viewModel.loadBookmarkStatus()
        else viewModel.clearBookmarkState()
    }
    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) onSessionExpired(uiState.resumeBookmark)
    }
    LaunchedEffect(saveAfterLogin, isLoggedIn, uiState.isSaved, uiState.isCheckingBookmark) {
        if (saveAfterLogin && isLoggedIn && uiState.isSaved != null && !uiState.isCheckingBookmark) {
            if (uiState.isSaved == false) viewModel.toggleBookmark()
            onSaveHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }

            Text(
                text = "Đọc bài báo",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge
            )
            if (isLoggedIn) {
                IconButton(
                    onClick = { viewModel.toggleBookmark() },
                    enabled = uiState.article != null && uiState.isSaved != null &&
                        !uiState.isCheckingBookmark && !uiState.isUpdatingBookmark
                ) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isSaved == true) R.drawable.ic_bookmark_filled
                            else R.drawable.ic_bookmark_outline
                        ),
                        contentDescription = if (uiState.isSaved == true) "Bỏ lưu bài báo" else "Lưu bài để đọc sau",
                        tint = if (uiState.isSaved == true) MaterialTheme.colorScheme.primary
                               else LocalContentColor.current
                    )
                }
            } else {
                TextButton(onClick = onLoginClick) { Text("Đăng nhập") }
            }
        }

        if (isLoggedIn && uiState.bookmarkError.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    uiState.bookmarkError,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = { viewModel.loadBookmarkStatus() }) { Text("Thử lại") }
            }
        }

        when {
            uiState.isLoading -> {
                Text(
                    text = "Đang tải nội dung...",
                    modifier = Modifier.padding(16.dp)
                )
            }

            uiState.errorMessage.isNotEmpty() -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = { viewModel.loadArticle(url) }
                    ) {
                        Text("Thử lại")
                    }
                }
            }

            else -> {
                val article = uiState.article

                if (article != null) {
                    key(article) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false

                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView,
                                            request: WebResourceRequest
                                        ): Boolean {
                                            return true
                                        }
                                    }

                                    loadDataWithBaseURL(
                                        article.url,
                                        buildArticleHtml(article),
                                        "text/html",
                                        null,
                                        article.url
                                    )
                                }
                            },
                            onReset = null,
                            onRelease = { view ->
                                view.destroy()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun buildArticleHtml(article: ArticleDetail): String {
    val plainContent = htmlEncode(article.content)
        .replace("\n", "<br>")

    val body = article.content_html
        ?.takeIf { it.isNotBlank() }
        ?: "<p>$plainContent</p>"

    val information = listOfNotNull(
        article.category,
        article.published_at?.replace('T', ' ')?.take(16),
        Uri.parse(article.url).host
    ).joinToString(" · ")

    return """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport"
                  content="width=device-width, initial-scale=1">
            <meta http-equiv="Content-Security-Policy"
                  content="default-src 'none';
                           img-src https: http: data:;
                           style-src 'unsafe-inline';">
            <style>
                * { box-sizing: border-box; }

                body {
                    margin: 0;
                    padding: 16px;
                    background: white;
                    color: #172B27;
                    font-family: Georgia, "Times New Roman", serif;
                    font-size: 18px;
                    line-height: 1.7;
                    overflow-wrap: break-word;
                }

                h1 {
                    font-size: 26px;
                    line-height: 1.35;
                    margin: 0 0 12px;
                }

                article > h1.title-detail,
                article > p.description {
                    display: none;
                }

                p { margin: 0 0 16px; }

                .meta {
                    font-size: 13px;
                    color: #64748B;
                }

                .description { font-weight: bold; }

                img {
                    position: static !important;
                    display: block;
                    max-width: 100% !important;
                    height: auto !important;
                }

                figure, table, .tplCaption {
                    width: 100% !important;
                    max-width: 100% !important;
                    margin: 16px 0 !important;
                }

                figure {
                    display: block !important;
                    float: none !important;
                }

                .fig-picture {
                    width: 100% !important;
                    position: static !important;
                    padding-bottom: 0 !important;
                }

                picture { display: block; }

                figcaption { width: 100%; }

                figure img, table img {
                    display: block;
                    width: 100% !important;
                }

                figcaption, .Image, .caption {
                    font-size: 13px;
                    line-height: 1.5;
                    color: #64748B;
                }

                a {
                    color: #0F766E;
                    pointer-events: none;
                }
            </style>
        </head>
        <body>
            <h1>${htmlEncode(article.title)}</h1>
            <p class="meta">${htmlEncode(information)}</p>
            <p class="description">${htmlEncode(article.description)}</p>
            <article>$body</article>
        </body>
        </html>
    """.trimIndent()
}