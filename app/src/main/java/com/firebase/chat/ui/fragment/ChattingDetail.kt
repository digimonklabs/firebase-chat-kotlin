package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.addTextChangedListener
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
    private var receiverId = ""
    override val layoutId: Int
        get() = R.layout.fragment_chating_details
    override val viewModel: ChatDetailViewModel by viewModel()
    private val timer = object : CountDownTimer(3000L,3000L) {
        override fun onTick(p0: Long) {

        }

        override fun onFinish() {
            viewModel.setTyping(false)
        }
    }

    override fun setVariable() {
        binding.apply {
            chatViewModel = viewModel
            chatDetails = this@ChattingDetail
            adapter = ChatDetailsAdapter(mContext,viewModel.chatModel.get()!!,viewModel)
        }
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        NotificationManagerCompat.from(mContext).cancel(AppConstant.notificationID)
        receiverId = navArgs.receiverId
        Log.e("receiverId","receiverId -> $receiverId")
        viewModel.initChatId(receiverId,this)
        viewModel.setReceiverName(receiverId)
        handelTyping()
    }

    private fun handelTyping() {
        binding.edMessage.doAfterTextChanged {
            timer.cancel()
            it?.length?.let {
                if (it <= 0) {
                    viewModel.setTyping(false)
                }else {
                    viewModel.setTyping(true)
                    timer.start()
                }
            }
        }
    }

    fun onSendChat(view: View) {
        view.setDelay(2000)
        viewModel.sendMessage(receiverId)
    }

    override fun onSetAdapter() {
        binding.adapter = ChatDetailsAdapter(mContext,viewModel.chatModel.get()!!,viewModel)
        binding.adapter?.notifyItemRangeChanged(0,viewModel.chatModel.get()!!.size -1)
        binding.rvChatList.scrollToPosition(viewModel.chatModel.get()!!.size -1)
    }
}