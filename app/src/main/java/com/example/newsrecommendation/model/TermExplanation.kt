package com.example.newsrecommendation.model

data class TermExplanationRequest(val url: String, val term: String)
data class TermExplanationResponse(val term: String, val explanation: String, val source_url: String)
