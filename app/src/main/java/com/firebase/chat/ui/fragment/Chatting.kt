package com.firebase.chat.ui.fragment

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentChattingBinding
import com.firebase.chat.ui.adapter.ChatListAdapter
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.firebase.chat.utils.Extension.checkIsTiramisu
import com.firebase.chat.utils.Extension.checkPermission
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
            if (!it && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
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
            adapter = ChatListAdapter(this@Chatting.viewModel.userList.get()!!,this@Chatting.viewModel.friendUid.get()!!,this@Chatting.viewModel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun launchNotificationPermission() {
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

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

        viewModel.init(this)

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

    override fun onSetAdapter() {
        binding.adapter?.addItem(viewModel.userList.get()!!)
        binding.adapter?.notifyItemRangeChanged(0,viewModel.userList.get()!!.size-1)
    }
}