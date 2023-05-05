package com.firebase.chat.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.chat.databinding.ReceiverChatLayoutBinding
import com.firebase.chat.databinding.SenderChatLayoutBinding
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.model.firebasedb.ChatModel

class ChatDetailsAdapter(
    private val context: Context,
    private val chatMessage: ArrayList<ChatModel>,
    private val viewModel: ChatDetailViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENDER = 1
    private val VIEW_TYPE_RECEIVER = 2

    inner class SenderViewHolder(val binding: SenderChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(chatModel: ChatModel) {
            binding.tvSenderMessage.text = chatModel.message
            binding.tvDateTime.text = viewModel.getDateTime(chatModel.dateTime)
        }
    }

    inner class ReceiverViewHolder(val binding: ReceiverChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(chatModel: ChatModel) {
            binding.tvSenderMessage.text = chatModel.message
            binding.tvDateTime.text = viewModel.getDateTime(chatModel.dateTime)
        }
    }

    override fun getItemViewType(position: Int): Int {
        Log.e("PrintonSendChat","isSender ${chatMessage[chatMessage.size -1].chatId == Firebase.auth.uid}  ")
        return if (chatMessage[position].chatId == Firebase.auth.uid) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
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