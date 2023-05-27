package com.firebase.chat.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.firebase.chat.fcm.MyFirebaseMessagingService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.util.AppConstant
import org.koin.java.KoinJavaComponent.inject

class ReplayNotification : BroadcastReceiver() {
    private val chatDetailUseCase : ChatDetailUseCase by inject(ChatDetailUseCase::class.java,null,null)
    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.let {
            val chatId = it.getStringExtra(AppConstant.CHAT_ID)
            val notificationId = it.getStringExtra(AppConstant.NOTIFICATION_ID)
            val message = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(MyFirebaseMessagingService.KEY_TEXT_REPLY)
            val isFromReplay = it.getStringExtra(MyFirebaseMessagingService.REQUEST_CODE_REPLAY) == MyFirebaseMessagingService.REQUEST_CODE_REPLAY
            val isMarkAsRead = it.getStringExtra(MyFirebaseMessagingService.REQUEST_CODE_MARK_AS_READ) == MyFirebaseMessagingService.REQUEST_CODE_MARK_AS_READ
            Log.e("PrintRemoteMessage","message -> $message -- $chatId  $isFromReplay $isMarkAsRead $notificationId")

            if (isFromReplay) {
                chatId?.let { chat_id ->
                    val chat = ChatModel(
                        chatId = chat_id,
                        senderID = chatDetailUseCase.getFireBaseAuth().uid.toString(),
                        message = message.toString(),
                        dateTime = System.currentTimeMillis(),
                        read = true,
                        status = 2
                    )
                    chatDetailUseCase.getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chat_id).push().setValue(chat).addOnCompleteListener {
                        val receiver = chat_id.split("_")
                        val receiverId = if (receiver[0] == chatDetailUseCase.getFireBaseAuth().uid) receiver[1] else receiver[0]
                        val friendReference = chatDetailUseCase.getDataBaseReference().child(AppConstant.FRIEND_TABLE)
                            .child(chatDetailUseCase.getFireBaseAuth().uid.toString())
                        friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (sp in snapshot.children) {
                                    sp.getValue<Friends>()?.let {
                                        if (it.chatId == chatId) {
                                            friendReference.child(sp.key.toString()).child(AppConstant.LAST_MESSAGE)
                                                .setValue(message)
                                            friendReference.child(sp.key.toString()).child(AppConstant.TYPING)
                                                .setValue("")
                                            friendReference.child(sp.key.toString()).child(AppConstant.PENDING_COUNT)
                                                .setValue(0)
                                            friendReference.child(sp.key.toString()).child(AppConstant.DATE_TIME)
                                                .setValue(System.currentTimeMillis())
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                        val receiverReferenceReference =
                            chatDetailUseCase.getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)
                        receiverReferenceReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (sp in snapshot.children) {
                                    sp.getValue<Friends>()?.let {
                                        if (it.chatId == chatId) {
                                            receiverReferenceReference.child(sp.key.toString())
                                                .child(AppConstant.LAST_MESSAGE).setValue(message)
                                            receiverReferenceReference.child(sp.key.toString()).child(AppConstant.TYPING)
                                                .setValue("")
                                            receiverReferenceReference.child(sp.key.toString())
                                                .child(AppConstant.PENDING_COUNT).setValue(it.pendingCount + 1)
                                            receiverReferenceReference.child(sp.key.toString())
                                                .child(AppConstant.DATE_TIME).setValue(System.currentTimeMillis())
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            if (isMarkAsRead || isFromReplay) {
                chatId?.let { chat_id ->
                    val receiver = chatId.split("_")
                    val receiverId = if (receiver[0] == chatDetailUseCase.getFireBaseAuth().uid) receiver[1] else receiver[0]
                    val notificationManager = chatDetailUseCase.getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(AppConstant.notificationID)
                    val friendReference = chatDetailUseCase.getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(chatDetailUseCase.getFireBaseAuth().uid.toString())
                    friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (sp in snapshot.children) {
                                sp.getValue<Friends>()?.let {
                                    if (receiverId == it.friendUid) {
                                        friendReference.child(sp.key.toString()).child(AppConstant.PENDING_COUNT).setValue(0)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                    val chatReference = chatDetailUseCase.getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chat_id)
                    chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (sp in snapshot.children) {
                                sp.getValue(ChatModel::class.java)?.let {
                                    if (it.senderID != chatDetailUseCase.getFireBaseAuth().uid) {
                                        chatReference.child(sp.key.toString()).child(AppConstant.MESSAGE_READ).setValue(true)
                                        chatReference.child(sp.key.toString()).child(AppConstant.STATUS).setValue(3)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }
        }
    }
}