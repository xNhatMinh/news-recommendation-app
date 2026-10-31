package com.example.newsrecommendation.model

data class BookmarkRequest(val url: String)

data class BookmarkStatus(val is_saved: Boolean)

data class SavedArticlesResponse(
    val results: List<RecommendationItem>,
    val has_more: Boolean
)
