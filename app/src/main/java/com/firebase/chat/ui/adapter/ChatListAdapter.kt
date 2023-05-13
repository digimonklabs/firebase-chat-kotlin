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
import com.firebase.chat.utils.Extension.toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.TypingModel
import com.mobisharnam.domain.model.firebasedb.UidList
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant
import java.util.Calendar
import java.util.Locale
import org.json.JSONException

class ChatListAdapter(
    private val chatModel: ArrayList<User>,
    private val uid: ArrayList<NewChatModel>,
    viewModel: ChatListViewModel
) : BaseAdapters<ItemChattingListBinding, ChatListViewModel, NewChatModel>(uid, viewModel) {

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
        notifyItemRangeChanged(0,item.size - 1)
    }

    val calendar: Calendar = Calendar.getInstance()
    override val layoutId: Int
        get() = R.layout.item_chatting_list

    override fun bind(
        binding: ItemChattingListBinding,
        item: NewChatModel,
        position: Int,
        holder: BaseViewHolder<ItemChattingListBinding>,
        viewModel: ChatListViewModel
    ) {
        Log.e("PrintOnBind","PrintOnBind")
        binding.apply {
            isMessagePending = false
            mBinding = this
            this.viewModel = viewModel
            userModel = item
            onlineDot.background = if (item.isChatOnline) ContextCompat.getDrawable(
                onlineDot.context,
                R.drawable.green_dot
            ) else ContextCompat.getDrawable(onlineDot.context, R.drawable.yellow_dot)
            if (item.senderID == viewModel.getFireBaseAuth().uid) {
                tvUserName.text = item.receiverName
                try {
                    val name = item.receiverName.split(" ")
                    ivUserImage.text = name[0][0] + name[1][0].toString()
                } catch (e: Exception) {
                    ivUserImage.text = item.receiverName
                }
            }else {
                tvUserName.text = item.senderName
                try {
                    val name = item.senderName.split(" ")
                    ivUserImage.text = name[0][0] + name[1][0].toString()
                } catch (e: Exception) {
                    ivUserImage.text = item.senderName
                }
            }

            if (item.chatId.split("_")[0] != viewModel.getFireBaseAuth().uid) {
                isMessagePending = item.senderPendingMessage != 0
                pendingMessageCount = item.senderPendingMessage.toString()
                tvLastMessage.text = if (item.senderTyping) root.context.getString(R.string.alert_typing) else item.message
                Log.e("PrintPendingMessage"," senderPendingMessage -> ${item.senderPendingMessage}")
            } else {
                isMessagePending = item.receiverPendingMessage != 0
                pendingMessageCount = item.receiverPendingMessage.toString()
                tvLastMessage.text = if (item.receiverTyping) root.context.getString(R.string.alert_typing) else item.message
                Log.e("PrintPendingMessage"," receiverPendingMessage -> ${item.receiverPendingMessage}")
            }

            val yesterDayCalender = Calendar.getInstance()
            val lastTwoDayCalender = Calendar.getInstance()
            yesterDayCalender.add(Calendar.DATE, -1)
            lastTwoDayCalender.add(Calendar.DATE, -2)

            calendar.timeInMillis = item.dateTime
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
        }

        holder.itemView.setOnClickListener {
            val status = if (item.isChatOnline) {
                binding.root.context.getString(R.string.online)
            } else {
                binding.tvTime.text.toString()
            }
            viewModel.navigate(
                ChattingDirections.chattingToChattingDetailFragment(
                    item.chatId,
                    status,
                    0
                )
            )
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