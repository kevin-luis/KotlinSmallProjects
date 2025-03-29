package com.cobacoba.mygenai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobacoba.mygenai.model.Message
import com.cobacoba.mygenai.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Tambahkan variable untuk model yang dipilih
    private val _selectedModelId = MutableLiveData<String>("llama3-70b-8192") // Default model
    val selectedModelId: LiveData<String> = _selectedModelId

    // Fungsi untuk mengubah model yang dipilih
    fun setSelectedModel(modelId: String) {
        _selectedModelId.value = modelId
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = Message(
            content = content,
            isFromUser = true
        )

        // Tambahkan pesan pengguna ke daftar pesan
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(userMessage)
        _messages.value = currentMessages

        // Kirim pesan ke API dengan model yang dipilih
        _isLoading.value = true
        viewModelScope.launch {
            repository.sendMessage(
                userMessage = content,
                messageHistory = _messages.value ?: emptyList(),
                modelId = _selectedModelId.value ?: "llama3-70b-8192" // Gunakan model yang dipilih
            )
                .onSuccess { aiResponse ->
                    val aiMessage = Message(
                        content = aiResponse,
                        isFromUser = false
                    )
                    val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
                    updatedMessages.add(aiMessage)
                    _messages.value = updatedMessages
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Terjadi kesalahan"
                }

            _isLoading.value = false
        }
    }

    // Untuk mengatur pesan dari file yang dimuat
    fun setMessages(messages: List<Message>) {
        _messages.value = messages
    }

    // Untuk menghapus semua pesan
    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}