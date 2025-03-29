package com.cobacoba.mygenai.adapter

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cobacoba.mygenai.R
import com.cobacoba.mygenai.model.Message
import io.noties.markwon.Markwon

class ChatAdapter : ListAdapter<Message, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private var onItemLongClickListener: ((Message, View) -> Boolean)? = null

    private var contextMenuPosition = -1

    fun getContextMenuPosition(): Int = contextMenuPosition


    fun setOnItemLongClickListener(listener: (Message, View) -> Boolean) {
        onItemLongClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

        holder.itemView.setOnLongClickListener { view ->
            contextMenuPosition = holder.adapterPosition
            onItemLongClickListener?.invoke(getItem(position), view) ?: false
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener, View.OnLongClickListener {

        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageCardView: CardView = itemView.findViewById(R.id.messageCardView)
        private val markwon = Markwon.create(itemView.context)

        init {
            itemView.setOnCreateContextMenuListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(message: Message) {
            senderTextView.text = if (message.isFromUser) "Anda" else "AI"

            // Render Markdown pada pesan
            markwon.setMarkdown(messageTextView, message.content)

            if (message.isFromUser) {
                // Styling untuk pesan pengguna
                messageCardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.user_message_bg)
                )
                messageTextView.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.user_message_text)
                )
            } else {
                // Styling untuk pesan AI
                messageCardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.ai_message_bg)
                )
                messageTextView.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.ai_message_text)
                )
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            contextMenuPosition = adapterPosition  // Pastikan posisi tersimpan

            menu?.add(0, R.id.action_copy, 0, "Salin Pesan")
        }


        override fun onLongClick(v: View?): Boolean {
            v?.showContextMenu()  // Pastikan context menu muncul
            return true  // Harus return true agar event tetap berjalan
        }

    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}