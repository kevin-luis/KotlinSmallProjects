package com.cobacoba.mygenai.model

data class GroqRequestBody(
    val model: String = "llama3-70b-8192", // atau model lain yang tersedia di Groq
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)