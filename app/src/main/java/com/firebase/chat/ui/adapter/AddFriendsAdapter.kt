package com.firebase.chat.ui.adapter

import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemAddFriendsBinding
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.mobisharnam.domain.model.firebasedb.UidList
import com.mobisharnam.domain.model.firebasedb.User
import java.util.Locale
import org.json.JSONException

class AddFriendsAdapter(
    private val userModel: ArrayList<User>,
    viewModel: ChatListViewModel,
    private val uid: ArrayList<UidList>
) : BaseAdapters<ItemAddFriendsBinding, ChatListViewModel, User>(userModel, viewModel) {

    private val allUserList: java.util.ArrayList<User> =
        java.util.ArrayList<User>()
    private val filterUserList: java.util.ArrayList<User> =
        java.util.ArrayList<User>()

    override val layoutId: Int
        get() = R.layout.item_add_friends

    fun addItem(item: ArrayList<User>) {
        this.allUserList.addAll(item)
        this.filterUserList.addAll(item)
        notifyDataSetChanged()
    }

    override fun bind(
        binding: ItemAddFriendsBinding,
        item: User,
        position: Int,
        holder: BaseViewHolder<ItemAddFriendsBinding>,
        viewModel: ChatListViewModel
    ) {
        binding.apply {
            userModel = item
        }
        holder.itemView.setOnClickListener {
            binding.checkUser.isChecked = !binding.checkUser.isChecked
        }
        binding.checkUser.setOnCheckedChangeListener { compoundButton, isCheck ->
            when (isCheck) {
                true -> {
//                    viewModel.friendList.get()?.add(uid)
                }

                false -> {
//                    viewModel.friendList.get()?.remove(uid[position])
                }
            }
        }
    }

    fun filter(charText: String) {
        val charText1 = charText.lowercase(Locale.getDefault())
        userModel.clear()
        if (charText1.isEmpty()) {
            userModel.addAll(allUserList)
        } else {
            for (i in allUserList.indices) {
                try {
                    val text1 = filterUserList[i].userName
                    if (text1.lowercase(Locale.getDefault()).contains(charText)) {
                        userModel.add(filterUserList[i])
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        notifyDataSetChanged()
    }
}