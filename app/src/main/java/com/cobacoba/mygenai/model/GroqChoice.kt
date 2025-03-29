package com.cobacoba.mygenai.model

data class GroqChoice(
    val index: Int,
    val message: GroqMessage,
    val finish_reason: String
)