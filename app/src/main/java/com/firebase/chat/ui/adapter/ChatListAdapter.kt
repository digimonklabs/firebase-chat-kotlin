package com.firebase.chat.ui.adapter

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
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import java.util.Calendar
import java.util.Locale
import org.json.JSONException

class ChatListAdapter(
    private val chatModel: ArrayList<NewUser>,
    private var uid: ArrayList<NewChatModel>,
    viewModel: ChatListViewModel
) : BaseAdapters<ItemChattingListBinding, ChatListViewModel, NewChatModel>(uid, viewModel) {

    lateinit var mBinding: ItemChattingListBinding
    private val allUserList: java.util.ArrayList<NewUser> =
        java.util.ArrayList<NewUser>()
    private val filterUserList: java.util.ArrayList<NewUser> =
        java.util.ArrayList<NewUser>()

    fun addItem(item: ArrayList<NewUser>) {
        allUserList.clear()
        filterUserList.clear()
        this.allUserList.addAll(item)
        this.filterUserList.addAll(item)
        notifyItemRangeChanged(0, item.size - 1)
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
        Log.e("PrintOnBind", "PrintOnBind")
        binding.apply {
           // isMessagePending = false
            mBinding = this
            this.viewModel = viewModel
            userModel = item
            onlineDot.background = if (true) ContextCompat.getDrawable(
                onlineDot.context,
                R.drawable.green_dot
            ) else ContextCompat.getDrawable(onlineDot.context, R.drawable.yellow_dot)

            if (item.chatId.split("_")[0] == viewModel.getFireBaseAuth().uid) {
                tvLastMessage.text = if (item.receiverTyping) root.context.getString(R.string.alert_typing) else item.message
                isMessagePending = item.receiverPendingMessage != 0
                pendingMessageCount = item.receiverPendingMessage.toString()
            }else {
                tvLastMessage.text = if (item.senderTyping) root.context.getString(R.string.alert_typing) else item.message
                isMessagePending = item.senderPendingMessage != 0
                pendingMessageCount = item.senderPendingMessage.toString()
            }

            /*if (item.chatId.split("_")[0] != viewModel.getFireBaseAuth().uid) {
                viewModel.getDataBaseReference().child(AppConstant.TYPING_TABLE).child(item.chatId)
                    .child(AppConstant.SENDER_TYPING)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.getValue<Boolean>()?.let { isTyping ->
                                    tvLastMessage.text =
                                        if (isTyping) root.context.getString(R.string.alert_typing) else item.message
                                    Log.e("PrintBooleanValue", "PrintBooleanValue 1-> $isTyping -- ${item.message}")
                                }
                            }else {
                                tvLastMessage.text = ""
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                viewModel.getDataBaseReference().child(AppConstant.PENDING_MESSAGE_TABLE)
                    .child(item.chatId)
                    .child(AppConstant.SENDER_PENDING_MESSAGE)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.getValue<Int>()?.let { count ->
                                    isMessagePending = count != 0
                                    pendingMessageCount = count.toString()
                                    Log.e("PrintBooleanValue", "count 1-> $count")
                                }
                            }else {
                                isMessagePending = false
                                pendingMessageCount = ""
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            } else {
                viewModel.getDataBaseReference().child(AppConstant.TYPING_TABLE).child(item.chatId)
                    .child(AppConstant.RECEIVER_TYPING)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.getValue<Boolean>()?.let { isTyping ->
                                    tvLastMessage.text =
                                        if (isTyping) root.context.getString(R.string.alert_typing) else item.message
                                    Log.e("PrintBooleanValue", "PrintBooleanValue 2-> $isTyping -- ${item.message}")
                                }
                            }else {
                                tvLastMessage.text = ""
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                viewModel.getDataBaseReference().child(AppConstant.PENDING_MESSAGE_TABLE)
                    .child(item.chatId)
                    .child(AppConstant.RECEIVER_PENDING_MESSAGE)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                           if (snapshot.exists()) {
                               snapshot.getValue<Int>()?.let { count ->
                                   isMessagePending = count != 0
                                   pendingMessageCount = count.toString()
                                   Log.e("PrintBooleanValue", "count 1-> $count")
                               }
                           }else {
                               isMessagePending = false
                               pendingMessageCount = ""
                           }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }*/

            if (item.chatId.split("_")[0] == viewModel.getFireBaseAuth().uid) {
                tvUserName.text = item.receiverName
                try {
                    val name = item.receiverName.split(" ")
                    ivUserImage.text = name[0][0] + name[1][0].toString()
                } catch (e: Exception) {
                    ivUserImage.text = item.receiverName
                }
            } else {
                tvUserName.text = item.senderName
                try {
                    val name = item.senderName.split(" ")
                    ivUserImage.text = name[0][0] + name[1][0].toString()
                } catch (e: Exception) {
                    ivUserImage.text = item.senderName
                }
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
            val status = if (true) {
                binding.root.context.getString(R.string.online)
            } else {
                binding.tvTime.text.toString()
            }
            /*viewModel.navigate(
                ChattingDirections.chattingToChattingDetailFragment(
                    item.chatId,
                    status,
                    0,
                    uid[position].senderName,
                    uid[position].receiverName,
                )
            )*/
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