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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
            adapter = AddFriendsAdapter(this@AddUser.viewModel.userList.get()!!,this@AddUser.viewModel,this@AddUser.viewModel.friendUid.get()!!)
        }
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        viewModel.init(this)

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
        val rootReferences = viewModel.getDataBaseReference()
        rootReferences.child(AppConstant.USER_TABLE).child(Firebase.auth.uid.toString()).child(
            AppConstant.USER_FRIEND_LIST).setValue(viewModel.friendList.get())
        viewModel.friendList.get()?.forEach {
//            rootReferences.child(AppConstant.USER_TABLE).child(it).child(AppConstant.USER_FRIEND_LIST).setValue(Firebase.auth.uid.toString())
        }
    }

    override fun onSetAdapter() {
        binding.adapter?.addItem(viewModel.userList.get()!!)
        binding.adapter?.notifyDataSetChanged()
    }
}