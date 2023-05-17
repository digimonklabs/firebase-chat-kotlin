package com.firebase.chat.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.chat.databinding.ReceiverChatLayoutBinding
import com.firebase.chat.databinding.SenderChatLayoutBinding
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.NewChatModel

class ChatDetailsAdapter(
    private val context: Context,
    private val chatMessage: ArrayList<ChatModel>,
    private val viewModel: ChatDetailViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENDER = 1
    private val VIEW_TYPE_RECEIVER = 2

    inner class SenderViewHolder(val binding: SenderChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(chatModel: ChatModel) {
            if (chatModel.message.isNotEmpty()) {
                binding.tvSenderMessage.visibility = View.VISIBLE
                binding.tvDateTime.visibility = View.VISIBLE
                binding.tvSenderMessage.text = chatModel.message
                binding.tvDateTime.text = viewModel.getDateTime(chatModel.dateTime)
            } else {
                binding.tvSenderMessage.visibility = View.GONE
                binding.tvDateTime.visibility = View.GONE
            }
        }
    }

    inner class ReceiverViewHolder(val binding: ReceiverChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(chatModel: ChatModel) {
            if (chatModel.message.isNotEmpty()) {
                binding.tvSenderMessage.visibility = View.VISIBLE
                binding.tvDateTime.visibility = View.VISIBLE
                binding.tvSenderMessage.text = chatModel.message
                binding.tvDateTime.text = viewModel.getDateTime(chatModel.dateTime)
            } else {
                binding.tvSenderMessage.visibility = View.GONE
                binding.tvDateTime.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessage[position].senderID == Firebase.auth.uid) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val binding = SenderChatLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
                SenderViewHolder(binding)
            }
            VIEW_TYPE_RECEIVER -> {
                val binding = ReceiverChatLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
                ReceiverViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SENDER -> {
                val senderViewHolder = holder as SenderViewHolder
                senderViewHolder.bindData(chatMessage[position])
            }
            VIEW_TYPE_RECEIVER -> {
                val receiverViewHolder = holder as ReceiverViewHolder
                receiverViewHolder.bindData(chatMessage[position])
            }
        }
    }

    override fun getItemCount(): Int = chatMessage.size

}