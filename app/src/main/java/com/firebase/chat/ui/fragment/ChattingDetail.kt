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
import com.firebase.chat.fcm.MyFirebaseMessagingService
import com.firebase.chat.ui.adapter.ChatDetailsAdapter
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.firebase.chat.utils.Extension.setDelay
import com.firebase.chat.utils.Extension.toast
import com.google.firebase.messaging.FirebaseMessaging
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChattingDetail : BaseFragment<FragmentChatingDetailsBinding, ChatDetailViewModel>(),
    OnSetAdapter {

    private val chatListModel = ArrayList<ChatModel>()
    private val navArgs: ChattingDetailArgs by navArgs()
    private var chatId = ""
    override val layoutId: Int
        get() = R.layout.fragment_chating_details
    override val viewModel: ChatDetailViewModel by viewModel()
    private val timer = object : CountDownTimer(3000L, 3000L) {
        override fun onTick(p0: Long) {

        }

        override fun onFinish() {
            viewModel.setTyping(false, chatId)
            viewModel.setTypingId(false, chatId)
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

        setUpChatObserver()
        viewModel.allowRead.set(true)
        AppConstant.isRead = true
        chatId = navArgs.chatId

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            Log.e("PrintUSerToken", "ff token -> ${it.result} token -> ${navArgs.token}")
            it.result
        }

        MyFirebaseMessagingService.CHAT_ID = chatId
        viewModel.getToken(chatId)
        viewModel.getNotificationId(chatId)
        viewModel.setUserStatus(chatId)
        viewModel.setTyping()
        viewModel.setMarkAsRead(chatId)
        viewModel.setSender(navArgs.userName)
        viewModel.getChat(chatId)
        handelTyping()
    }

    private fun setUpChatObserver() {
        viewModel.chatLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Response.Status.SUCCESS -> {
                        it.data?.let { chatList ->
                            chatListModel.addAll(chatList)
                            binding.adapter = ChatDetailsAdapter(mContext, chatList, viewModel)
                            binding.adapter?.notifyItemRangeChanged(0, chatList.size - 1)
                            binding.rvChatList.scrollToPosition(chatList.size - 1)
                        }
                    }

                    Response.Status.ERROR -> {
                        it.message?.let { mContext.toast(it) }
                    }

                    Response.Status.EXCEPTION -> {
                        it.message?.let { mContext.toast(it) }
                    }
                }
            }
        }
    }

    private fun handelTyping() {
        binding.edMessage.doAfterTextChanged {
            timer.cancel()
            it?.length?.let {
                if (it <= 0) {
                    viewModel.setTyping(false, chatId)
                    viewModel.setTypingId(false, chatId)
                } else {
                    viewModel.setTyping(true, chatId)
                    viewModel.setTypingId(false, chatId)
                    viewModel.setTypingId(true, chatId)
                    timer.start()
                }
            }
        }
    }

    fun onSendChat(view: View) {
        val message = viewModel.chatMessage.get()
        if (message.isNullOrEmpty()) {
            return
        } else if (message.toString().trim().isEmpty()) {
            return
        }
        view.setDelay(2000)

        viewModel.sendNotificationID(navArgs.notificationId, chatId)
        val chatItem = chatListModel[chatListModel.size - 1]
        viewModel.sendMessage(chatId, chatItem, navArgs.token)
    }

    override fun onSetAdapter(adapter: String) {
       /* if (adapter == AppConstant.CHAT_DETAILS_ADAPTER_ADAPTER) {
            binding.adapter =
                ChatDetailsAdapter(mContext, viewModel.chatListModel.get()!!, viewModel)
            binding.adapter?.notifyItemRangeChanged(0, viewModel.chatListModel.get()!!.size - 1)
            binding.rvChatList.scrollToPosition(viewModel.chatListModel.get()!!.size - 1)
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.allowRead.set(false)
    }
}