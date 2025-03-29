package com.cobacoba.mygenai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobacoba.mygenai.adapter.ChatAdapter
import com.cobacoba.mygenai.databinding.ActivityMainBinding
import com.cobacoba.mygenai.model.Message
import com.cobacoba.mygenai.utility.ChatSaveUtils
import com.cobacoba.mygenai.viewmodel.ChatViewModel
import io.noties.markwon.Markwon

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var modelNames: Array<String>
    private lateinit var modelIds: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar as ActionBar
        setSupportActionBar(binding.toolbar)

        setupModelSpinner()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Cek apakah ada chat yang akan dimuat
        intent.getStringExtra("LOAD_CHAT_FILE")?.let { fileName ->
            loadSavedChat(fileName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_chat -> {
                showSaveChatDialog()
                true
            }
            R.id.action_load_chat -> {
                openSavedChats()
                true
            }
            R.id.action_clear_chat -> {
                showClearChatDialog()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupModelSpinner() {
        // Dapatkan array model dari resources
        modelNames = resources.getStringArray(R.array.ai_models)
        modelIds = resources.getStringArray(R.array.ai_model_ids)

        // Buat adapter untuk spinner
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            modelNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set adapter ke spinner
        binding.modelSpinner.adapter = spinnerAdapter

        // Atur listener untuk pemilihan model
        binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedModelId = modelIds[position]
                viewModel.setSelectedModel(selectedModelId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak perlu implementasi
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.message_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_copy -> {
                val position = chatAdapter.getContextMenuPosition()
                if (position != -1) {
                    val message = viewModel.messages.value?.getOrNull(position)
                    message?.let { copyMessageToClipboard(it) }
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }


    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
        }

        // Setup long click listener for copy
        chatAdapter.setOnItemLongClickListener { message, view ->
            view.setOnCreateContextMenuListener(this@MainActivity)
            view.showContextMenu()
            true
        }
    }
    private fun stripMarkdown(text: String): String {
        return text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")  // Bold -> **text** jadi text
            .replace(Regex("_([^_]*)_"), "$1")          // Italic -> _text_ jadi text
            .replace(Regex("`([^`]*)`"), "$1")          // Inline code -> `code` jadi code
            .replace(Regex("~~(.*?)~~"), "$1")         // Strikethrough -> ~~text~~ jadi text
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // Link -> [text](url) jadi text
            .replace(Regex("(?m)^#{1,6}\\s*(.*?)\$"), "$1") // Heading -> ## Header jadi Header
    }

    private fun stripTags(text: String): String {
        return text.replace(Regex("<[^>]+>"), "") // Hapus semua tag HTML/AI seperti <think>...</think>
    }

    private fun cleanText(text: String): String {
        val noTags = stripTags(text)  // Hapus tag AI
        return stripMarkdown(noTags)  // Hapus format Markdown
    }




    private fun copyMessageToClipboard(message: Message) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Bersihkan teks dari tag dan Markdown
        val cleanText = cleanText(message.content)

        val clip = ClipData.newPlainText("Message", cleanText)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Pesan disalin", Toast.LENGTH_SHORT).show()
    }




    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.messageEditText.text?.clear()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages.toList()) // Gunakan .toList() untuk memicu DiffUtil
            if (messages.isNotEmpty()) {
                binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.sendButton.isEnabled = !isLoading
            binding.messageEditText.isEnabled = !isLoading
            binding.modelSpinner.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showSaveChatDialog() {
        if (viewModel.messages.value.isNullOrEmpty()) {
            Toast.makeText(this, "Tidak ada percakapan untuk disimpan", Toast.LENGTH_SHORT).show()
            return
        }

        val editText = EditText(this)
        editText.hint = "Nama chat (opsional)"

        AlertDialog.Builder(this)
            .setTitle("Simpan Percakapan")
            .setView(editText)
            .setPositiveButton("Simpan") { _, _ ->
                val customName = editText.text.toString().trim()
                val fileName = ChatSaveUtils.saveChat(this, viewModel.messages.value!!, customName)
                Toast.makeText(this, "Percakapan disimpan: $fileName", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openSavedChats() {
        val intent = Intent(this, SavedChatsActivity::class.java)
        startActivity(intent)
    }

    private fun loadSavedChat(fileName: String) {
        val messages = ChatSaveUtils.loadChat(this, fileName)
        if (messages != null) {
            viewModel.setMessages(messages)
            Toast.makeText(this, "Chat dimuat", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Gagal memuat chat", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showClearChatDialog() {
        if (viewModel.messages.value.isNullOrEmpty()) {
            Toast.makeText(this, "Chat sudah kosong", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Hapus Percakapan")
            .setMessage("Apakah Anda yakin ingin menghapus semua pesan? Ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.clearMessages()
                Toast.makeText(this, "Chat dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
