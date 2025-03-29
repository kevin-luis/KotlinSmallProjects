package com.cobacoba.mygenai.model

import com.google.gson.annotations.SerializedName

data class GroqResponse(
    val id: String,
    val model: String,
    @SerializedName("object") val objectType: String, // Gunakan nama variabel yang berbeda
    val created: Long,
    val choices: List<GroqChoice>
)