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
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant
import java.util.Calendar

class ChatDetailViewModel(private val chatDetailUseCase: ChatDetailUseCase) :
    BaseViewModel(chatDetailUseCase) {

    val chatModel = ObservableField(ArrayList<ChatModel>())
    val chatMessage = ObservableField("")
    val chatId = ObservableField("")
    val receiverName = ObservableField("")
    val userImageName = ObservableField("")
    val userStatus = ObservableField("")

    fun getDateTime(timeStamp: Long): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = timeStamp
        return dateFormat.format(calender.timeInMillis)
    }

    fun sendMessage(receiverId: String, notificationId: Int) {
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
            chatDetailUseCase.getSendNotification(receiverId, message, chatId.get()!!,notificationId)
        }.addOnFailureListener {
            Log.e("chatMessage", "chatMessage fail to send")
        }
    }

    fun getUserChat(onSetAdapter: OnSetAdapter) {
        val chatArray = ArrayList<ChatModel>()
        val reference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId.get()!!)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatModel.get()?.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        i.getValue(ChatModel::class.java)?.let {
                            chatArray.add(it)
                        }
                        chatModel.set(chatArray)
                        onSetAdapter.onSetAdapter()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setReceiverName(receiverID: String) {
        val chatReferences = getDataBaseReference().child(AppConstant.USER_TABLE)
        chatReferences.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (sp in snapshot.children) {
                        if (sp.key == receiverID) {
                            sp.getValue(User::class.java)?.let {
                                receiverName.set(it.userName)
                                try {
                                    val name = it.userName.split(" ")
                                    userImageName.set(name[0][0] + name[1][0].toString())
                                } catch (e: Exception) {
                                    userImageName.set(it.userName[0].toString())
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun initChatId(receiverId: String, onSetAdapter: OnSetAdapter) {
        val senderChatId = "${getFireBaseAuth().uid}_${receiverId}"
        val receiverChatId = "${receiverId}_${getFireBaseAuth().uid}"
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount == 0L) {
                    chatId.set(senderChatId)
                    getUserChat(onSetAdapter)
                    setMarkAsRead(receiverId)
                } else {
                    for (ds in snapshot.children) {
                        Log.e(
                            "PrintOnDataChange",
                            "onDataChange -> ${ds.key}  --  ${snapshot.childrenCount}"
                        )
                        if (ds.key != null) {
                            if (ds.key!! == senderChatId) {
                                chatId.set(senderChatId)
                                getUserChat(onSetAdapter)
                                setMarkAsRead(receiverId)
                            } else if (ds.key!! == receiverChatId) {
                                chatId.set(receiverChatId)
                                getUserChat(onSetAdapter)
                                setMarkAsRead(receiverId)
                            }
                        } else {
                            if (snapshot.children.last() == ds.children) {
                                chatId.set(senderChatId)
                                getUserChat(onSetAdapter)
                                setMarkAsRead(receiverId)
                            }
                        }
                    }
                    if (chatId.get().isNullOrEmpty()) {
                        chatId.set(senderChatId)
                        getUserChat(onSetAdapter)
                        setMarkAsRead(receiverId)
                    }
                }
                Log.e("GetChatID", "chatId -> ${chatId.get()}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setMarkAsRead(receiverId: String) {
        clearNotification(receiverId)
        val chatReference =
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId.get()!!)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (sp in snapshot.children) {
                        sp.getValue<ChatModel>()?.let {
                            if (it.chatId != getFireBaseAuth().uid && AppConstant.isRead) {
                                Log.e("chatReference", "uid -> ${sp.key}")
                                chatReference.child(sp.key.toString())
                                    .child(AppConstant.MESSAGE_READ).setValue(true)
                                    .addOnSuccessListener {
                                        Log.e("chatReference", "chatReference -> ")
                                    }.addOnFailureListener {
                                        Log.e("chatReference", "chatReference -> ${it.message}")
                                    }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setTyping(isTyping: Boolean) {
        Log.e("chatId", "chatId -> ${chatId.get()}")
        getDataBaseReference().child("/${AppConstant.TYPING_TABLE}/${chatId.get()}")
            .child(AppConstant.TYPE).setValue(isTyping)
    }

    fun sendNotificationID(notificationId: Int, receiverId: String) {
        getDataBaseReference().child(AppConstant.NOTIFICATION_TABLE).child("${receiverId}_${getFireBaseAuth().uid}").setValue(notificationId)
    }

    fun clearNotification(receiverId: String) {
        val notificationReference = getDataBaseReference().child(AppConstant.NOTIFICATION_TABLE)
        notificationReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationManager = chatDetailUseCase.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                snapshot.child("${getFireBaseAuth().uid}_$receiverId").getValue<Int>()?.let {
                    notificationManager.cancel(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}