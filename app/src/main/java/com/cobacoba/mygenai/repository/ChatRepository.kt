package com.cobacoba.mygenai.repository


import com.cobacoba.mygenai.api.GroqApiClient
import com.cobacoba.mygenai.model.GroqMessage
import com.cobacoba.mygenai.model.GroqRequestBody
import com.cobacoba.mygenai.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val apiService = GroqApiClient.apiService

    suspend fun sendMessage(
        userMessage: String,
        messageHistory: List<Message>,
        modelId: String = "llama3-70b-8192" // Parameter tambahan untuk model
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Konversi history percakapan ke format yang diharapkan oleh Groq API
                val groqMessages = messageHistory.map {
                    GroqMessage(
                        role = if (it.isFromUser) "user" else "assistant",
                        content = it.content
                    )
                }.toMutableList()

                // Tambahkan pesan pengguna baru
                groqMessages.add(GroqMessage(role = "user", content = userMessage))

                // Panggil API Groq dengan model yang dipilih
                val apiKey = "Bearer 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" // Ganti dengan API key Anda
                val response = apiService.generateCompletion(
                    authorization = apiKey,
                    requestBody = GroqRequestBody(
                        model = modelId, // Gunakan model yang dipilih
                        messages = groqMessages
                    )
                )

                if (response.isSuccessful) {
                    val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content
                    if (aiResponse != null) {
                        Result.success(aiResponse)
                    } else {
                        Result.failure(Exception("Tidak ada respons dari AI"))
                    }
                } else {
                    Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}