package com.firebase.chat.ui.viewmodel

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.data.source.remote.settings.Setting
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.FriendsList
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import java.util.Calendar

class ChatDetailViewModel(private val chatDetailUseCase: ChatDetailUseCase) :
    BaseViewModel(chatDetailUseCase) {

    val chatModel = ObservableField(ArrayList<ChatModel>())
    val chatListModel = ObservableField(ArrayList<NewChatModel>())
    val chatMessage = ObservableField("")
    val chatId = ObservableField("")
    val receiverName = ObservableField("")
    val userImageName = ObservableField("")
    val userStatus = ObservableField("")
    val senderPendingMessageCount = ObservableField(0)
    val receiverPendingMessageCount = ObservableField(0)

    fun getDateTime(timeStamp: Long): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = timeStamp
        return dateFormat.format(calender.timeInMillis)
    }

    fun sendMessage1(receiverId: String, notificationId: Int) {
        val message = chatMessage.get()
        val references =
            getDataBaseReference().child("/${AppConstant.CHAT_TABLE}/${chatId.get()}")
                .push()
        val lastMessage = mapOf(
            receiverId to FriendsList(
                lastMessage = message.toString(),
                dateTime = System.currentTimeMillis()
            )
        )
        getDataBaseReference().child(AppConstant.USER_TABLE).child(getFireBaseAuth().uid.toString())
            .child(AppConstant.FRIENDS_LIST).updateChildren(lastMessage)

        references.orderByKey().limitToLast(1).ref.removeValue()
        val chatModel = ChatModel(
            chatMessage.get().toString(),
            getFireBaseAuth().uid.toString(),
            System.currentTimeMillis(),
            false,
            ""
        )

        chatMessage.set("")

        references.setValue(chatModel).addOnCompleteListener {
            val header = HashMap<String, String>()
            header[AppConstant.AUTHORIZATION] = AppConstant.SERVER_KEY
            header[AppConstant.CONTENT_TYPE] = AppConstant.APPLICATION_JSON
            Setting.HEADER = header
            chatDetailUseCase.getSendNotification(
                receiverId,
                message,
                chatId.get()!!,
                notificationId
            )
        }.addOnFailureListener {
            Log.e("chatMessage", "chatMessage fail to send")
        }
    }

    fun getChat(chatId: String, setAdapter: OnSetAdapter) {
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatListModel.set(ArrayList())
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        sp.getValue(NewChatModel::class.java)?.let {
                            chatListModel.get()?.add(it)
                        }
                    }
                    setAdapter.onSetAdapter(AppConstant.CHAT_DETAILS_ADAPTER_ADAPTER)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setMarkAsRead(chatId: String) {
        val messageReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue(NewChatModel::class.java)?.let {
                        if (it.senderID == getFireBaseAuth().uid) {
                            messageReference.child(sp.key.toString())
                                .child(AppConstant.MESSAGE_READ).setValue(true)
                        }
                        if (chatId.split("_")[0] != getFireBaseAuth().uid) {
                            messageReference.child(sp.key.toString()).child("senderPendingMessage")
                                .setValue(0)
                        } else {
                            messageReference.child(sp.key.toString())
                                .child("receiverPendingMessage").setValue(0)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setTyping(isTyping: Boolean, chatId: String) {
        getDataBaseReference().child("/${AppConstant.TYPING_TABLE}/${this.chatId.get()}")
            .child(AppConstant.TYPE).setValue(isTyping)
        val typingReference =
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId).orderByKey()
                .limitToLast(1)
        typingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue(NewChatModel::class.java)?.let {
                        if (chatId.split("_")[0] == getFireBaseAuth().uid) {
                            typingReference.ref.child(sp.key.toString()).child(AppConstant.SENDER_TYPING)
                                .setValue(isTyping)
                        } else {
                            typingReference.ref.child(sp.key.toString()).child(AppConstant.RECEIVER_TYPING)
                                .setValue(isTyping)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun sendNotificationID(notificationId: Int, receiverId: String) {
        getDataBaseReference().child(AppConstant.NOTIFICATION_TABLE)
            .child("${receiverId}_${getFireBaseAuth().uid}").setValue(notificationId)
    }

    fun clearNotification(receiverId: String) {
        val notificationReference = getDataBaseReference().child(AppConstant.NOTIFICATION_TABLE)
        notificationReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationManager = chatDetailUseCase.getContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                snapshot.child("${getFireBaseAuth().uid}_$receiverId").getValue<Int>()?.let {
                    notificationManager.cancel(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setUserName(chatId: String) {
        val receiver = chatId.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) receiver[1] else receiver[0]

        getDataBaseReference().child("USerTable").child(getFireBaseAuth().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(NewUser::class.java)?.let { user ->
                        val chatReference =
                            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
                        chatReference.orderByKey().limitToLast(1)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (sp in snapshot.children) {
                                        sp.getValue(NewChatModel::class.java)?.let {
                                            chatReference.child(sp.key.toString())
                                                .child("senderName").setValue(user.userName)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        getDataBaseReference().child("USerTable").child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(NewUser::class.java)?.let { user ->
                        val chatReference =
                            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
                        chatReference.orderByKey().limitToLast(1)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (sp in snapshot.children) {
                                        sp.getValue(NewChatModel::class.java)?.let {
                                            chatReference.child(sp.key.toString())
                                                .child("receiverName").setValue(user.userName)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun sendMessage(chatID: String, notificationId: Int) {
        val message = chatMessage.get()
        val references =
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatID)
                .push()
        val receiver = chatID.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) {
            senderPendingMessageCount.set(senderPendingMessageCount.get()?.plus(1))
            receiver[1]
        } else {
            receiverPendingMessageCount.set(receiverPendingMessageCount.get()?.plus(1))
            receiver[0]
        }

        setUserName(chatID)
        val chat = NewChatModel(
            message = message.toString(),
            senderID = getFireBaseAuth().uid.toString(),
            dateTime = System.currentTimeMillis(),
            read = false,
            senderTyping = false,
            receiverTyping = false,
            isChatOnline = true,
            senderName = "",
            receiverName = "",
            chatId = chatID,
            senderPendingMessage = senderPendingMessageCount.get() ?: 0,
            receiverPendingMessage = receiverPendingMessageCount.get() ?: 0
        )

        chatMessage.set("")

        references.setValue(chat).addOnCompleteListener {
            references.orderByKey().limitToLast(1)
            val header = HashMap<String, String>()
            header[AppConstant.AUTHORIZATION] = AppConstant.SERVER_KEY
            header[AppConstant.CONTENT_TYPE] = AppConstant.APPLICATION_JSON
            Setting.HEADER = header
            chatDetailUseCase.getSendNotification(receiverId, message, chatID, notificationId)
        }.addOnFailureListener {
            Log.e("chatMessage", "chatMessage fail to send")
        }
    }

    fun getPendingMessage(chatId: String) {
        val bothId = chatId.split("_")
        getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    senderPendingMessageCount.set(0)
                    receiverPendingMessageCount.set(0)
                    var receiverCount = 0
                    var senderCount = 0
                    for (sp in snapshot.children) {
                        sp.getValue(NewChatModel::class.java)?.let {
                            if (!it.read) {
                                if (bothId[0] == it.senderID) {
                                    senderCount++
                                } else {
                                    receiverCount++
                                }
                            }
                        }
                    }
                    senderPendingMessageCount.set(senderCount)
                    receiverPendingMessageCount.set(receiverCount)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}