package com.cobacoba.mygenai.api

import com.cobacoba.mygenai.model.GroqRequestBody
import com.cobacoba.mygenai.model.GroqResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") authorization: String,
        @Body requestBody: GroqRequestBody
    ): Response<GroqResponse>
}