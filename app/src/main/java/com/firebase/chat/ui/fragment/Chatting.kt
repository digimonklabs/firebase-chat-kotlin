package com.firebase.chat.ui.fragment

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.callback.OnAdapterChange
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentChattingBinding
import com.firebase.chat.ui.adapter.ChatListAdapter
import com.firebase.chat.ui.adapter.UserListAdapter
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.firebase.chat.utils.Extension.checkIsTiramisu
import com.firebase.chat.utils.Extension.checkPermission
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class Chatting : BaseFragment<FragmentChattingBinding, ChatListViewModel>(), OnSetAdapter,
    OnAdapterChange {

    private val chatList = ArrayList<NewChatModel>()
    private val friendsList = ArrayList<Friends>()
    var searchString = ""
    override val layoutId: Int
        get() = R.layout.fragment_chatting
    override val viewModel: ChatListViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
//                launchNotificationPermission()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                launchNotificationPermission()
            } else {
                val intent = Intent()
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(AppConstant.APP_PACKAGE, mContext.packageName)
                intent.putExtra(AppConstant.APP_UID, mContext.applicationInfo.uid)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.packageName)
                startActivity(intent)
            }
        }

    override fun setVariable() {
        binding.apply {
            chatFragment = this@Chatting
            viewModel = this@Chatting.viewModel
            adapter = UserListAdapter(
                this@Chatting.viewModel.userList.get()!!,
                friendsList,
                this@Chatting.viewModel
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun launchNotificationPermission() {
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  viewModel.getAllUser()
        viewModel.getFriends(this)

    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        AppConstant.isRead = false
        viewModel.initUserChat(this,this)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        })

        if (mContext.checkIsTiramisu()) {
            if (!mContext.checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                launchNotificationPermission()
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    fun onAddFriend() {
        viewModel.navigate(ChattingDirections.addUserToChattingFragment())
    }

    var check = true
    override fun onSetAdapter(adapter: String) {
        friendsList.clear()
        friendsList.addAll(viewModel.friendList)
        binding.adapter?.notifyDataSetChanged()
    }

    override fun onAdapterChange(position: Int, startPosition: Int, endPosition: Int) {
       /* chatList.clear()
        viewModel.chatList.get()?.let {
            chatList.addAll(it)
        }
        if (position == -1) {
            binding.adapter = ChatListAdapter(
                this@Chatting.viewModel.userList.get()!!,
                chatList,
                this@Chatting.viewModel
            )
        }else {
            binding.adapter?.notifyItemChanged(position)
        }
        if (startPosition != -1 && endPosition != -1) {
            binding.adapter?.notifyItemRangeChanged(startPosition,endPosition)
        }*/
        Log.e("PrintPOsitions","position ->  $position - $startPosition - $endPosition")
    }
}