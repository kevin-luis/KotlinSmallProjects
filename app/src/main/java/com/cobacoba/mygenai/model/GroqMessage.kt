package com.cobacoba.mygenai.model

data class GroqMessage(
    val role: String, // "user" atau "assistant"
    val content: String
)