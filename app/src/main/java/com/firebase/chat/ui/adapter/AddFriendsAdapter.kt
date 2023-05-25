package com.firebase.chat.ui.adapter

import android.util.Log
import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemAddFriendsBinding
import com.firebase.chat.ui.viewmodel.AddFriendsViewModel
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import java.util.Locale
import org.json.JSONException

class AddFriendsAdapter(
    private val userModel: ArrayList<NewUser>,
    viewModel: AddFriendsViewModel
) : BaseAdapters<ItemAddFriendsBinding, AddFriendsViewModel, NewUser>(userModel, viewModel) {

    private val allUserList: java.util.ArrayList<NewUser> =
        java.util.ArrayList<NewUser>()
    private val filterUserList: java.util.ArrayList<NewUser> =
        java.util.ArrayList<NewUser>()

    override val layoutId: Int
        get() = R.layout.item_add_friends

    fun addItem(item: ArrayList<NewUser>) {
        this.allUserList.addAll(item)
        this.filterUserList.addAll(item)
        notifyDataSetChanged()
    }

    override fun bind(
        binding: ItemAddFriendsBinding,
        item: NewUser,
        position: Int,
        holder: BaseViewHolder<ItemAddFriendsBinding>,
        viewModel: AddFriendsViewModel
    ) {
        Log.e("PrintINAdapter","user.data -> ${item.userName}")
        binding.apply {
            userModel = item
        }
        holder.itemView.setOnClickListener {
            binding.checkUser.isChecked = !binding.checkUser.isChecked
        }
        binding.checkUser.setOnCheckedChangeListener { compoundButton, isCheck ->
            when (isCheck) {
                true -> {
                    val invitation = Invitation(userModel[position].uid,userModel[position].userName)
                    viewModel.sendInvitationList.get()?.add(invitation)
                }

                false -> {
                    val invitation = Invitation(userModel[position].uid,userModel[position].userName)
                    viewModel.sendInvitationList.get()?.remove(invitation)
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