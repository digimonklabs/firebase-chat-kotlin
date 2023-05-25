package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentAddUserBinding
import com.firebase.chat.ui.adapter.AddFriendsAdapter
import com.firebase.chat.ui.viewmodel.AddFriendsViewModel
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.response.Response
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddUser : BaseFragment<FragmentAddUserBinding, AddFriendsViewModel>() {

    private val userList = ArrayList<NewUser>()
    private var searchString = ""
    override val layoutId: Int
        get() = R.layout.fragment_add_user
    override val viewModel: AddFriendsViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            viewModel = this@AddUser.viewModel
            addUser = this@AddUser
            adapter = AddFriendsAdapter(userList, this@AddUser.viewModel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllUser()
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        setUpFriendsObserver()

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

    private fun setUpFriendsObserver() {
        viewModel.friendsLiveData.observe(viewLifecycleOwner) { response ->
            response.getContentIfNotHandled()?.let { user ->
                when (user.status) {
                    Response.Status.SUCCESS -> {
                        userList.clear()
                        user.data?.let {
                            userList.addAll(it)
                            binding.adapter?.addItem(it)
                        }
                        binding.adapter?.notifyItemRangeChanged(0,userList.size - 1)
                    }

                    Response.Status.ERROR -> {
                        mContext.toast(user.message.toString())
                    }

                    Response.Status.EXCEPTION -> {
                        mContext.toast(user.message.toString())
                    }
                }
            }
        }
    }

    fun onAddClick() {
        viewModel.sendInvitation()
    }
}