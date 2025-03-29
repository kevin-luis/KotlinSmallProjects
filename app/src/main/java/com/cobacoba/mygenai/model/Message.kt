package com.cobacoba.mygenai.model

data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)