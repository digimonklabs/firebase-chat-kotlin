package com.firebase.chat.ui.adapter

import android.util.Log
import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemChattingListBinding
import com.firebase.chat.ui.fragment.ChattingDirections
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.NewUser
import java.util.Calendar
import java.util.Locale
import org.json.JSONException

class UserListAdapter(
    private val chatModel: ArrayList<NewUser>,
    private var friends: ArrayList<Friends>,
    viewModel: ChatListViewModel
) : BaseAdapters<ItemChattingListBinding, ChatListViewModel, Friends>(friends, viewModel) {

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

    private val calendar: Calendar = Calendar.getInstance()
    override val layoutId: Int
        get() = R.layout.item_chatting_list

    override fun bind(
        binding: ItemChattingListBinding,
        item: Friends,
        position: Int,
        holder: BaseViewHolder<ItemChattingListBinding>,
        viewModel: ChatListViewModel
    ) {
        Log.e("PrintOnBind", "PrintOnBind ${item.pendingCount} ${item.chatId}")
        binding.apply {
            tvUserName.text = item.name
            tvLastMessage.text = item.lastMessage
            tvLastMessage.text = if (item.typing != viewModel.getFireBaseAuth().uid && item.typing.isNotEmpty()) root.context.getString(R.string.alert_typing) else item.lastMessage
            isMessagePending = item.pendingCount != 0
            pendingMessageCount = item.pendingCount.toString()
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
            val name = item.name.split(" ")
            try {
                ivUserImage.text = name[0][0] + name[1][0].toString()
            } catch (e: Exception) {
                ivUserImage.text = name.first().toString()
            }
        }

        holder.itemView.setOnClickListener {
            val status = if (true) {
                binding.root.context.getString(R.string.online)
            } else {
                binding.tvTime.text.toString()
            }
            viewModel.navigate(
                ChattingDirections.chattingToChattingDetailFragment(
                    item.chatId,
                    item.notificationId,
                    item.name,
                    item.token,
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