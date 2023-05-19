package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentChatingDetailsBinding
import com.firebase.chat.ui.adapter.ChatDetailsAdapter
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.firebase.chat.utils.Extension.setDelay
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChattingDetail : BaseFragment<FragmentChatingDetailsBinding, ChatDetailViewModel>(),
    OnSetAdapter {

    private val navArgs: ChattingDetailArgs by navArgs()
    private var chatId = ""
    override val layoutId: Int
        get() = R.layout.fragment_chating_details
    override val viewModel: ChatDetailViewModel by viewModel()
    private val timer = object : CountDownTimer(3000L, 3000L) {
        override fun onTick(p0: Long) {

        }

        override fun onFinish() {
            Log.e("onFinish", "onFinish")
            viewModel.setTyping(false,chatId)
        }
    }

    override fun setVariable() {
        binding.apply {
            chatViewModel = viewModel
            chatDetails = this@ChattingDetail
            adapter = ChatDetailsAdapter(mContext, viewModel.chatListModel.get()!!, viewModel)
        }
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        AppConstant.isRead = true
        chatId = navArgs.chatId
//        viewModel.setMarkAsRead(chatId)
        viewModel.setMarkAsRead(chatId)
        viewModel.setSender(navArgs.userName)
        viewModel.clearNotification(chatId)
        viewModel.getChat(chatId,this)
        handelTyping()
    }

    private fun handelTyping() {
        binding.edMessage.doAfterTextChanged {
            timer.cancel()
            it?.length?.let {
                if (it <= 0) {
                    viewModel.setTyping(false, chatId)
                } else {
                    viewModel.setTyping(true, chatId)
                    timer.start()
                }
            }
        }
    }

    fun onSendChat(view: View) {
        val message = viewModel.chatMessage.get()
        if (message.isNullOrEmpty()) {
            return
        }else if (message.toString().trim().isEmpty()) {
            return
        }
        view.setDelay(2000)
        viewModel.sendNotificationID(navArgs.notificationId,chatId)
//        viewModel.sendMessage(chatId,navArgs.notificationId,navArgs.userName)
        viewModel.sendMessage(chatId,navArgs.notificationId,navArgs.userName)
    }

    override fun onSetAdapter(adapter: String) {
        if (adapter == AppConstant.CHAT_DETAILS_ADAPTER_ADAPTER) {
            binding.adapter = ChatDetailsAdapter(mContext, viewModel.chatListModel.get()!!, viewModel)
            binding.adapter?.notifyItemRangeChanged(0, viewModel.chatListModel.get()!!.size - 1)
            binding.rvChatList.scrollToPosition(viewModel.chatListModel.get()!!.size - 1)
        }
    }
}