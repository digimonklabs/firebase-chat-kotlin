package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentAddUserBinding
import com.firebase.chat.ui.adapter.AddFriendsAdapter
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddUser : BaseFragment<FragmentAddUserBinding, ChatListViewModel>(), OnSetAdapter {

    private var searchString = ""
    override val layoutId: Int
        get() = R.layout.fragment_add_user
    override val viewModel: ChatListViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            viewModel = this@AddUser.viewModel
            addUser = this@AddUser
            adapter = AddFriendsAdapter(this@AddUser.viewModel.userChatList.get()!!,this@AddUser.viewModel,this@AddUser.viewModel.existFriendList.get()!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllUser(this)
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

       // viewModel.init(this)

        binding.searchFriend.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    searchString = p0
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    binding.adapter?.filter(p0.trim().lowercase())
                }
                return false
            }
        })
    }

    fun onAddClick() {
        viewModel.sendInvitation()
    }

    override fun onSetAdapter(adapter: String) {
        if (adapter == AppConstant.ADD_FRIENDS_ADAPTER) {
            binding.adapter?.addItem(viewModel.userChatList.get()!!)
            binding.adapter?.notifyDataSetChanged()
        }
    }
}