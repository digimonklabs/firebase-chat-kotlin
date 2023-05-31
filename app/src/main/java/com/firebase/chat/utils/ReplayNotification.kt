package com.firebase.chat.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import com.firebase.chat.fcm.MyFirebaseMessagingService
import com.mobisharnam.data.source.remote.settings.Setting
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ReplayNotification : BroadcastReceiver() {
    private val chatDetailUseCase: ChatDetailUseCase by inject(
        ChatDetailUseCase::class.java,
        null,
        null
    )

    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.let {
            val chatId = it.getStringExtra(AppConstant.CHAT_ID)
            val notificationId = it.getStringExtra(AppConstant.NOTIFICATION_ID)
            val receiverNotificationId = it.getStringExtra(AppConstant.RECEIVER_NOTIFICATION_ID)
            val userToken = it.getStringExtra(AppConstant.USER_TOKEN)
            val friendToken = it.getStringExtra(AppConstant.FRIEND_TOKEN)
            val message = RemoteInput.getResultsFromIntent(intent)
                ?.getCharSequence(MyFirebaseMessagingService.KEY_TEXT_REPLY)
            val isFromReplay =
                it.getStringExtra(MyFirebaseMessagingService.REQUEST_CODE_REPLAY) == MyFirebaseMessagingService.REQUEST_CODE_REPLAY
            val isMarkAsRead =
                it.getStringExtra(MyFirebaseMessagingService.REQUEST_CODE_MARK_AS_READ) == MyFirebaseMessagingService.REQUEST_CODE_MARK_AS_READ
            Log.e(
                "PrintRemoteMessage",
                "message -> $message -- $chatId  $isFromReplay $isMarkAsRead $notificationId"
            )

            chatId?.let { chat_id ->
                val chat = ChatModel(
                    chatId = chat_id,
                    senderID = chatDetailUseCase.getFireBaseAuth().uid.toString(),
                    message = message.toString(),
                    dateTime = System.currentTimeMillis(),
                    read = true,
                    status = 2
                )
                chatDetailUseCase.getDataBaseReference().child(AppConstant.CHAT_TABLE)
                    .child(chat_id).push().setValue(chat).addOnCompleteListener {

                    val receiver = chat_id.split("_")
                    val receiverId =
                        if (receiver[0] == chatDetailUseCase.getFireBaseAuth().uid) receiver[1] else receiver[0]

                    CoroutineScope(Dispatchers.IO).launch {
                        chatDetailUseCase.updateCurrentUserStatus(chat_id, message.toString())
                        chatDetailUseCase.updateFriendStatus(
                            chat_id,
                            message.toString(),
                            receiverId
                        )
                        chatDetailUseCase.setChatRead(chat_id)
                        val header = HashMap<String, String>()
                        header[AppConstant.AUTHORIZATION] = AppConstant.SERVER_KEY
                        header[AppConstant.CONTENT_TYPE] = AppConstant.APPLICATION_JSON
                        Setting.HEADER = header
                        chatDetailUseCase.getSendNotification(
                            receiverId,
                            message.toString(),
                            chat_id,
                            notificationId?.toInt() ?: 0,
                            receiverNotificationId?.toInt() ?: 0,
                            userToken ?: "",
                            friendToken ?: ""
                        )
                        chatDetailUseCase.allowRead = true
                        chatDetailUseCase.setMarkAsRead(chat_id).collect {
                            chatDetailUseCase.allowRead = false
                        }
                    }
                }
            }
        }
    }
}