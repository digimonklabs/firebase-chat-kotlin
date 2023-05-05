package com.firebase.chat.ui.adapter

import androidx.core.content.ContextCompat
import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemChattingListBinding
import com.firebase.chat.ui.fragment.ChattingDirections
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.mobisharnam.domain.model.firebasedb.User
import java.util.Locale
import org.json.JSONException

class ChatListAdapter(
    private val chatModel: ArrayList<User>,
    private val uid: ArrayList<String>,
    viewModel: ChatListViewModel
) : BaseAdapters<ItemChattingListBinding, ChatListViewModel, User>(chatModel, viewModel) {

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
            this.viewModel = viewModel
            userModel = item
            onlineDot.background = if (item.online) ContextCompat.getDrawable(
                onlineDot.context,
                R.drawable.green_dot
            ) else ContextCompat.getDrawable(onlineDot.context, R.drawable.yellow_dot)
        }

        viewModel.setTyping(item.uid)
        viewModel.getPendingMessage(item.uid)
        viewModel.getLastChatAndTime(item.uid)

        holder.itemView.setOnClickListener {
            viewModel.navigate(ChattingDirections.chattingToChattingDetailFragment(uid[position]))
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