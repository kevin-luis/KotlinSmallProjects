package com.cobacoba.mygenai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cobacoba.mygenai.utility.ChatSaveUtils

class SavedChatsActivity : AppCompatActivity() {

    private lateinit var chatListView: ListView
    private lateinit var chatFiles: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_chats)

        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat Tersimpan"

        chatListView = findViewById(R.id.chatListView)
        loadSavedChats()

        // Set item click listener
        chatListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = chatFiles[position]
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("LOAD_CHAT_FILE", selectedFile)
            startActivity(intent)
        }

        // Set long click listener untuk menghapus
        chatListView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedFile = chatFiles[position]
            showDeleteDialog(selectedFile)
            true
        }
    }

    private fun loadSavedChats() {
        chatFiles = ChatSaveUtils.loadSavedChatList(this)

        // Format nama file untuk ditampilkan (hapus ekstensi .json)
        val displayNames = chatFiles.map {
            it.removeSuffix(".json").replace("_", " ").replace("chat ", "Chat ")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        chatListView.adapter = adapter
    }

    private fun showDeleteDialog(fileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Chat")
            .setMessage("Apakah Anda yakin ingin menghapus chat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                if (ChatSaveUtils.deleteChat(this, fileName)) {
                    Toast.makeText(this, "Chat dihapus", Toast.LENGTH_SHORT).show()
                    loadSavedChats() // Reload list
                } else {
                    Toast.makeText(this, "Gagal menghapus chat", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}