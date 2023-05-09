package com.firebase.chat.ui.adapter

import android.graphics.Typeface
import android.util.Log
import androidx.core.content.ContextCompat
import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemChattingListBinding
import com.firebase.chat.ui.fragment.ChattingDirections
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.TypingModel
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant
import java.util.Calendar
import java.util.Locale
import org.json.JSONException

class ChatListAdapter(
    private val chatModel: ArrayList<User>,
    private val uid: ArrayList<String>,
    viewModel: ChatListViewModel
) : BaseAdapters<ItemChattingListBinding, ChatListViewModel, User>(chatModel, viewModel) {

    lateinit var mBinding: ItemChattingListBinding
    private val allUserList: java.util.ArrayList<User> =
        java.util.ArrayList<User>()
    private val filterUserList: java.util.ArrayList<User> =
        java.util.ArrayList<User>()

    fun addItem(item: ArrayList<User>) {
        allUserList.clear()
        filterUserList.clear()
        this.allUserList.addAll(item)
        this.filterUserList.addAll(item)
        notifyDataSetChanged()
    }

    val calendar: Calendar = Calendar.getInstance()
    override val layoutId: Int
        get() = R.layout.item_chatting_list

    override fun bind(
        binding: ItemChattingListBinding,
        item: User,
        position: Int,
        holder: BaseViewHolder<ItemChattingListBinding>,
        viewModel: ChatListViewModel
    ) {
        binding.apply {
            mBinding = this
            this.viewModel = viewModel
            userModel = item
            onlineDot.background = if (item.online) ContextCompat.getDrawable(
                onlineDot.context,
                R.drawable.green_dot
            ) else ContextCompat.getDrawable(onlineDot.context, R.drawable.yellow_dot)
        }
        var chatId = ""
        val senderChatId = "${viewModel.getFireBaseAuth().uid}_${item.uid}"
        val receiverChatId = "${item.uid}_${viewModel.getFireBaseAuth().uid}"
        Log.e("PrintReference", "senderChatId -> $senderChatId  receiverChatId -> $receiverChatId")
        val chatReference = viewModel.getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (ds in snapshot.children) {
                        Log.e("PrintReference", "ds.key -> ${ds.key} ${snapshot.childrenCount}")
                        if (ds.key != null) {
                            if (ds.key!! == senderChatId) {
                                chatId = senderChatId
                            } else if (ds.key!! == receiverChatId) {
                                chatId = receiverChatId
                            }
                        } else {
                            chatId = senderChatId
                        }
                    }
                    Log.e("PrintReference", "chatId 11 -> $chatId")


                    // For get pending message count
                    var pendingMessageList = 0
                    val messageCountReference =
                        viewModel.getDataBaseReference()
                            .child("/${AppConstant.CHAT_TABLE}/${chatId}")
                    val postMessageListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            pendingMessageList = 0
                            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                for (ds in dataSnapshot.children) {
                                    ds.getValue<ChatModel>()?.let {
                                        if (it.chatId != viewModel.getFireBaseAuth().uid) {
                                            if (!it.read) {
                                                pendingMessageList += 1
                                            }
                                        }
                                    }
                                }
                                binding.pendingMessageCount = "$pendingMessageList"
                                binding.isMessagePending = pendingMessageList != 0
                                Log.e(
                                    "PrintReference",
                                    "after -> $pendingMessageList ${binding.isMessagePending} ${binding.pendingMessageCount}"
                                )
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    }
                    if (chatId.isNotEmpty()) {
                        messageCountReference.addValueEventListener(postMessageListener)
                    }


                    // for show Typing.. alert message to receiver
                    Log.e("PrintReference", "typingReference  -> $chatId")
                    val typingReference =
                        viewModel.getDataBaseReference().child(AppConstant.TYPING_TABLE)
                            .child(chatId)
                    val typingListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue<TypingModel>()?.let {
                                if (it.type) {
                                    binding.tvLastMessage.text =
                                        binding.root.context.getString(R.string.alert_typing)
                                    binding.tvLastMessage.setTextColor(
                                        ContextCompat.getColor(
                                            binding.tvLastMessage.context,
                                            R.color.colorGreen
                                        )
                                    )
                                    binding.tvLastMessage.typeface = Typeface.DEFAULT_BOLD
                                } else {
                                    getLastChat(binding, chatId)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    }
                    if (chatId.isNotEmpty()) {
                        typingReference.addValueEventListener(typingListener)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        holder.itemView.setOnClickListener {
            val status = if (item.online) {
                binding.root.context.getString(R.string.online)
            } else {
                binding.tvTime.text.toString()
            }
            viewModel.navigate(
                ChattingDirections.chattingToChattingDetailFragment(
                    item.uid,
                    status
                )
            )
        }
    }

    private fun getLastChat(binding: ItemChattingListBinding, chatId: String) {
        val postReference =
            viewModel.getDataBaseReference()
                .child("/${AppConstant.CHAT_TABLE}/${chatId}")
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (sp in snapshot.children) {
                        sp.getValue(ChatModel::class.java)?.let {
                            val yesterDayCalender = Calendar.getInstance()
                            val lastTwoDayCalender = Calendar.getInstance()
                            yesterDayCalender.add(Calendar.DATE, -1)
                            lastTwoDayCalender.add(Calendar.DATE, -2)

                            calendar.timeInMillis = it.dateTime
                            val chatTime = viewModel.dateFormat.format(calendar.time)

                            if (calendar.timeInMillis in yesterDayCalender.timeInMillis..lastTwoDayCalender.timeInMillis) {
                                binding.tvTime.text =
                                    binding.root.context.getString(R.string.yesterday)
                            } else if (calendar.timeInMillis < lastTwoDayCalender.timeInMillis) {
                                binding.tvTime.text = chatTime
                            } else {
                                binding.tvTime.text =
                                    viewModel.timeFormat.format(calendar.timeInMillis)
                            }

                            binding.tvLastMessage.text = it.message
                            binding.tvLastMessage.setTextColor(binding.tvTime.textColors.defaultColor)
                            binding.tvLastMessage.typeface = Typeface.DEFAULT

                            Log.e(
                                "PrintMessage",
                                "it.message -> ${it.message}  ${binding.tvTime.textColors.defaultColor}"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        if (chatId.isNotEmpty()) {
            postReference.orderByKey().limitToLast(1).addValueEventListener(postListener)
        }
    }

    fun filter(charText: String) {
        val charText1 = charText.lowercase(Locale.getDefault())
        chatModel.clear()
        if (charText1.isEmpty()) {
            chatModel.addAll(allUserList)
        } else {
            for (i in allUserList.indices) {
                try {
                    val text1 = filterUserList[i].userName
                    if (text1.lowercase(Locale.getDefault()).contains(charText)) {
                        chatModel.add(filterUserList[i])
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        notifyDataSetChanged()
    }

}