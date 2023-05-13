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
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentChattingBinding
import com.firebase.chat.ui.adapter.ChatListAdapter
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.firebase.chat.utils.Extension.checkIsTiramisu
import com.firebase.chat.utils.Extension.checkPermission
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class Chatting : BaseFragment<FragmentChattingBinding, ChatListViewModel>(), OnSetAdapter {

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
           /* adapter = ChatListAdapter(
                this@Chatting.viewModel.userList.get()!!,
                this@Chatting.viewModel.chatList.get()!!,
                this@Chatting.viewModel
            )*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun launchNotificationPermission() {
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllUser()
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        AppConstant.isRead = false
        viewModel.initUserChat(this)
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

    override fun onSetAdapter(adapter: String) {
        if (adapter == AppConstant.CHAT_ADAPTER) {
            viewModel.chatList.get()?.sortByDescending {
                it.dateTime
            }
            Log.e("HOwmannyCall","onSetAdapter")
//            binding.adapter?.addItem(viewModel.userList.get()!!)
            binding.adapter = ChatListAdapter(
                this@Chatting.viewModel.userList.get()!!,
                this@Chatting.viewModel.chatList.get()!!,
                this@Chatting.viewModel
            )
        }
    }
}