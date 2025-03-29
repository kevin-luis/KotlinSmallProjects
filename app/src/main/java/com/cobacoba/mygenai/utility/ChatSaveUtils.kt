package com.cobacoba.mygenai.utility

import android.content.Context
import com.cobacoba.mygenai.model.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatSaveUtils {
    private const val CHATS_DIRECTORY = "saved_chats"

    // Simpan percakapan saat ini
    fun saveChat(context: Context, messages: List<Message>, customName: String? = null): String {
        val gson = Gson()
        val jsonString = gson.toJson(messages)

        // Buat direktori jika belum ada
        val chatDir = File(context.filesDir, CHATS_DIRECTORY)
        if (!chatDir.exists()) {
            chatDir.mkdirs()
        }

        // Buat nama file berdasarkan waktu atau nama kustom
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = if (customName.isNullOrBlank()) "chat_$timeStamp.json" else "$customName.json"

        // Tulis ke file
        val file = File(chatDir, fileName)
        file.writeText(jsonString)

        return fileName
    }

    // Muat daftar semua chat yang tersimpan
    fun loadSavedChatList(context: Context): List<String> {
        val chatDir = File(context.filesDir, CHATS_DIRECTORY)
        if (!chatDir.exists()) {
            return emptyList()
        }

        return chatDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".json") }
            ?.map { it.name }
            ?.sorted()
            ?.reversed()
            ?: emptyList()
    }

    // Muat percakapan tertentu
    fun loadChat(context: Context, fileName: String): List<Message>? {
        val file = File(File(context.filesDir, CHATS_DIRECTORY), fileName)
        if (!file.exists()) {
            return null
        }

        val gson = Gson()
        val jsonString = file.readText()
        val type = object : TypeToken<List<Message>>() {}.type

        return gson.fromJson(jsonString, type)
    }

    // Hapus percakapan yang tersimpan
    fun deleteChat(context: Context, fileName: String): Boolean {
        val file = File(File(context.filesDir, CHATS_DIRECTORY), fileName)
        return file.delete()
    }
}