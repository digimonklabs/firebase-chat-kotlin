package com.firebase.chat.ui.viewmodel

import android.app.NotificationManager
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.databinding.ObservableField
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.data.source.remote.settings.Setting
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatDetailViewModel(private val chatDetailUseCase: ChatDetailUseCase) :
    BaseViewModel(chatDetailUseCase) {

    private var chatId = ""
    val chatListModel = ObservableField(ArrayList<ChatModel>())
    val chatMessage = ObservableField("")
    val receiverName = ObservableField("")
    val userImageName = ObservableField("")
    val userStatus = ObservableField("")
    val pendingMessageCount = ObservableField(0)
    val allowRead = ObservableField(false)
    private val todayDateTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val yesterDayDateTime = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.ENGLISH)
    private val timer = object : CountDownTimer(3000L, 3000L) {
        override fun onTick(p0: Long) {

        }

        override fun onFinish() {
            setUserStatus(chatId)
        }
    }

    fun getDateTime(timeStamp: Long): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = timeStamp
        return timeFormat.format(calender.timeInMillis)
    }

    fun getChat(chatId: String, setAdapter: OnSetAdapter) {
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatListModel.set(ArrayList())
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        sp.getValue(ChatModel::class.java)?.let {
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

    fun setTyping(isTyping: Boolean, chatId: String) {
        val id = chatId.split("_")
        val receiverId = if (id[0] == getFireBaseAuth().uid) id[1] else id[0]
        val typingId = if (id[1] == getFireBaseAuth().uid) id[1] else id[0]

        val friendReference =
            getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)
        friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue<Friends>()?.let {
                        if (it.chatId == chatId) {
                            if (isTyping) {
                                friendReference.child(sp.key.toString()).child("typing")
                                    .setValue(typingId)
                            } else {
                                friendReference.child(sp.key.toString()).child("typing")
                                    .setValue("")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setTypingId(isTyping: Boolean, chatId: String) {
        val id = chatId.split("_")
        val receiverId = if (id[0] == getFireBaseAuth().uid) id[1] else id[0]
        val typingId = if (id[1] == getFireBaseAuth().uid) id[1] else id[0]

        val friendReference =
            getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)
        friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue<Friends>()?.let {
                        if (it.chatId == chatId) {
                            if (isTyping) {
                                friendReference.child(sp.key.toString()).child("typingId")
                                    .setValue(typingId)
                            } else {
                                friendReference.child(sp.key.toString()).child("typingId")
                                    .setValue("")
                            }
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

    fun getPendingMessage(chatId: String) {
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var receiverCount = 0
                var senderCount = 0
                for (sp in snapshot.children) {
                    sp.getValue<ChatModel>()?.let {
                        if (!it.read) {
                            if (getFireBaseAuth().uid == it.senderID) {
                                senderCount++
                            } else {
                                receiverCount++
                            }
                        }
                    }
                }
                pendingMessageCount.set(0)
                Log.e(
                    "PrintPendingCount",
                    "count -> $senderCount -- $receiverCount ${pendingMessageCount.get()}"
                )
                pendingMessageCount.set(receiverCount)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setSender(
        userName: String
    ) {
        receiverName.set(userName)
        try {
            val name = userName.split(" ")
            userImageName.set(name[0][0] + name[1][0].toString())
        } catch (e: Exception) {
            userImageName.set(userName.first().toString())
        }
    }

    fun sendMessage(chatId: String, notificationId: Int, userName: String) {
        val message = chatMessage.get()?.trim()
        val references =
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
                .push()
        val receiver = chatId.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) {
            //senderPendingMessageCount.set(senderPendingMessageCount.get()?.plus(1))
            receiver[1]
        } else {
            //receiverPendingMessageCount.set(receiverPendingMessageCount.get()?.plus(1))
            receiver[0]
        }
        val count = pendingMessageCount.get()?.plus(1)
        pendingMessageCount.set(0)
        pendingMessageCount.set(count)
        val chat = ChatModel(
            chatId = chatId,
            message = message.toString(),
            senderID = getFireBaseAuth().uid.toString(),
            dateTime = System.currentTimeMillis(),
            read = true,
            status = 2
        )

        chatMessage.set("")

        references.setValue(chat).addOnCompleteListener {
            val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
                .child(getFireBaseAuth().uid.toString())
            friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (sp in snapshot.children) {
                        sp.getValue<Friends>()?.let {
                            if (it.chatId == chatId) {
                                friendReference.child(sp.key.toString()).child("lastMessage")
                                    .setValue(message)
                                friendReference.child(sp.key.toString()).child("typing")
                                    .setValue("")
                                friendReference.child(sp.key.toString()).child("pendingCount")
                                    .setValue(0)
                                friendReference.child(sp.key.toString()).child("dateTime")
                                    .setValue(System.currentTimeMillis())
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            val receiverReferenceReference =
                getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)
            receiverReferenceReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (sp in snapshot.children) {
                        sp.getValue<Friends>()?.let {
                            if (it.chatId == chatId) {
                                receiverReferenceReference.child(sp.key.toString())
                                    .child("lastMessage").setValue(message)
                                receiverReferenceReference.child(sp.key.toString()).child("typing")
                                    .setValue("")
                                receiverReferenceReference.child(sp.key.toString())
                                    .child("pendingCount").setValue(it.pendingCount + 1)
                                receiverReferenceReference.child(sp.key.toString())
                                    .child("dateTime").setValue(System.currentTimeMillis())
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            val header = HashMap<String, String>()
            header[AppConstant.AUTHORIZATION] = AppConstant.SERVER_KEY
            header[AppConstant.CONTENT_TYPE] = AppConstant.APPLICATION_JSON
            Setting.HEADER = header
            // chatDetailUseCase.getSendNotification(receiverId, message, chatId, notificationId)
        }.addOnFailureListener {
            Log.e("chatMessage", "chatMessage fail to send")
        }
    }

    fun setMarkAsRead(chatId: String) {
        val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
            .child(getFireBaseAuth().uid.toString())
        friendReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue<Friends>()?.let {
                        if (it.chatId == chatId && allowRead.get() == true) {
                            friendReference.child(sp.key.toString()).child("pendingCount")
                                .setValue(0)
                        }
                    }
                }
                getPendingMessage(chatId)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue<ChatModel>()?.let {
                        if (it.senderID != getFireBaseAuth().uid && allowRead.get() == true) {
                            chatReference.child(sp.key.toString()).child("status").setValue(3)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun setTyping() {
        val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
            .child(getFireBaseAuth().uid.toString())
        friendReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("friendReference","friendReference")
                for (sp in snapshot.children) {
                    sp.getValue<Friends>()?.let {
                        if (it.typingId != getFireBaseAuth().uid && it.typingId.isNotEmpty()) {
                            timer.cancel()
                            timer.start()
                            userStatus.set(
                                chatDetailUseCase.getContext().getString(R.string.alert_typing)
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setUserStatus(chatId: String) {
        this.chatId = chatId
        val bothId = chatId.split("_")
        val receiverId = if (bothId[0] == getFireBaseAuth().uid) bothId[1] else bothId[0]
        getDataBaseReference().child(AppConstant.USER_TABLE).child(receiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<NewUser>()?.let {
                        val calendar = Calendar.getInstance()
                        val lastSeenCalender = Calendar.getInstance()
                        val todayCalender = Calendar.getInstance()
                        val yesterDayCalender = Calendar.getInstance()
                        lastSeenCalender.timeInMillis = it.lastSeen
                        yesterDayCalender.add(Calendar.DATE, -1)

                        calendar[Calendar.HOUR_OF_DAY] = 0
                        calendar[Calendar.MINUTE] = 0
                        calendar[Calendar.SECOND] = 0
                        calendar[Calendar.MILLISECOND] = 0

                        if (it.online) {
                            userStatus.set(
                                chatDetailUseCase.getContext().getString(R.string.online)
                            )
                        } else if (lastSeenCalender.timeInMillis > calendar.timeInMillis) {
                            val yesterDaySeen = todayDateTime.format(it.lastSeen)
                            userStatus.set(
                                "${
                                    chatDetailUseCase.getContext()
                                        .getString(R.string.last_seen_today)
                                } $yesterDaySeen"
                            )
                        } else if (lastSeenCalender.timeInMillis < calendar.timeInMillis) {
                            val lastSeen = todayDateTime.format(it.lastSeen)
                            userStatus.set(
                                "${
                                    chatDetailUseCase.getContext()
                                        .getString(R.string.last_seen_yesterday)
                                } $lastSeen"
                            )
                        } else if (lastSeenCalender.timeInMillis < yesterDayCalender.timeInMillis) {
                            val lastSeen = yesterDayDateTime.format(it.lastSeen)
                            userStatus.set(
                                "${
                                    chatDetailUseCase.getContext().getString(R.string.last_seen)
                                } $lastSeen"
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}