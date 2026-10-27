package com.example.newsrecommendation.model

data class ArticleDetail(
    val title: String,
    val description: String,
    val content: String,
    val category: String?,
    val image_url: String?,
    val published_at: String?,
    val url: String,
    val content_html: String? = null
)