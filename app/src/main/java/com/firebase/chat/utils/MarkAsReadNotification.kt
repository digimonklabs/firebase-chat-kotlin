package com.firebase.chat.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class MarkAsReadNotification : BroadcastReceiver() {

    private val chatDetailUseCase: ChatDetailUseCase by inject(
        ChatDetailUseCase::class.java,
        null,
        null
    )

    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.let {
            val chatId = it.getStringExtra(AppConstant.CHAT_ID)
            val notificationId = it.getStringExtra(AppConstant.NOTIFICATION_ID)
            chatId?.let { chat_id ->
                val receiver = chat_id.split("_")
                val receiverId =
                    if (receiver[0] == chatDetailUseCase.getFireBaseAuth().uid) receiver[1] else receiver[0]
                val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId?.toInt() ?: 0)

                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("MarkAsReadNotification","MarkAsReadNotification  $notificationId $chat_id")
                    chatDetailUseCase.allowRead = true
                    chatDetailUseCase.setMessageStatus(chat_id)
                    chatDetailUseCase.setMarkAsRead(chat_id).collect {
                        chatDetailUseCase.allowRead = false
                    }
                }
            }
        }
    }
}