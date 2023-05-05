package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.mobisharnam.domain.util.AppConstant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.FriendUID
import com.mobisharnam.domain.model.firebasedb.TypingModel
import com.mobisharnam.domain.model.firebasedb.User
import java.util.Calendar

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    private val calendar = Calendar.getInstance()
    val pendingMessageCount = ObservableField("0")
    val isMessagePending = ObservableBoolean(false)
    val lastChatTime = ObservableField("")
    val lastMessage = ObservableField("")
    val noFriend = ObservableBoolean(false)
    var userList = ObservableField(ArrayList<User>())
    var friendList = ObservableField(HashMap<String,FriendUID>())
    var friendUid = ObservableField(ArrayList<String>())

    fun init(onSetAdapter: OnSetAdapter): ArrayList<User> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    userList.get()?.clear()
                   // for (userSnapshot in dataSnapshot.children) {
                        val user = dataSnapshot.getValue(User::class.java)
                        user?.let {
                            if (dataSnapshot.key == getFireBaseAuth().uid) {
                                user.friendsList.forEach {
                                    Log.e("PrintUid","friendUid -> ${it.value.lastMessage}")
                                    friendUid.get()?.add(it.key)
                                }

                                userList.get()?.add(it)
                                friendList.get()?.clear()
//                                friendList.get()?[1].addAll(it.friendsList)
//                                friendList.set(it.friendsList)
                            }
                        }
                  //  }
                    friendUid.get()?.isEmpty()?.let { noFriend.set(it) }
                    onSetAdapter.onSetAdapter()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("postListener", "loadPost:onCancelled", databaseError.toException())
            }
        }
        val userRef = FirebaseDatabase.getInstance().getReference(AppConstant.USER_TABLE).child(getFireBaseAuth().uid.toString())
        userRef.addListenerForSingleValueEvent(postListener)
        Log.e("postListener", "userList.size ${userList.get()?.size}  ")
        return userList.get()!!
    }

    fun getPendingMessage(uid: String): Int {
        var chatId = ""
        var pendingMessageList = 0
        val senderChatId = "${getFireBaseAuth().uid}_${uid}"
        val receiverChatId = "${uid}_${getFireBaseAuth().uid}"
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    if (snapshot.childrenCount == 0L) {
                        chatId = senderChatId
                    } else {
                        for (ds in snapshot.children) {
                            if (ds.key != null) {
                                if (ds.key!! == senderChatId) {
                                    chatId = senderChatId
                                    break
                                } else if (ds.key!! == receiverChatId) {
                                    chatId = receiverChatId
                                    break
                                }
                            } else {
                                chatId = senderChatId
                                break
                            }
                        }
                    }

                    val postReference = getDataBaseReference().child("/${AppConstant.CHAT_TABLE}/${chatId}")
                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            pendingMessageList = 0
                            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                for (ds in dataSnapshot.children) {
                                    ds.getValue<ChatModel>()?.let {
                                        if (it.chatId != getFireBaseAuth().uid) {
                                            if (!it.read) {
                                                pendingMessageList += 1
                                            }
                                        }
                                    }
                                }
                                pendingMessageCount.set("$pendingMessageList")
                                if (pendingMessageList!= 0) {
                                    isMessagePending.set(true)
                                }else {
                                    isMessagePending.set(false)
                                }
                                Log.e("pendingMessageList", "after -> $pendingMessageList")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    }
                    postReference.addValueEventListener(postListener)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return pendingMessageList
    }

    fun getLastChatAndTime(uid: String) {
        var chatId = ""
        val senderChatId = "${getFireBaseAuth().uid}_${uid}"
        val receiverChatId = "${uid}_${getFireBaseAuth().uid}"
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (ds in snapshot.children) {
                        if (ds.key != null) {
                            if (ds.key!! == senderChatId) {
                                chatId = senderChatId
                                break
                            } else if (ds.key!! == receiverChatId) {
                                chatId = receiverChatId
                                break
                            }
                        } else {
                            chatId = senderChatId
                            break
                        }
                    }
                    Log.e("PrintChatTableId","chatId -> $chatId")

                    val postReference = getDataBaseReference().child("/${AppConstant.CHAT_TABLE}/${chatId}")
                    val postListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists() && snapshot.hasChildren()) {
                                for (sp in snapshot.children) {
                                    sp.getValue(ChatModel::class.java)?.let {
                                        val yesterDayCalender = Calendar.getInstance()
                                        val lastTwoDayCalender = Calendar.getInstance()
                                        yesterDayCalender.add(Calendar.DATE, -1)
                                        lastTwoDayCalender.add(Calendar.DATE, -2)

                                        calendar.timeInMillis = it.dateTime
                                        val chatTime = dateFormat.format(calendar.time)

                                        if (calendar.timeInMillis in yesterDayCalender.timeInMillis .. lastTwoDayCalender.timeInMillis) {
                                            lastChatTime.set("Yesterday")
                                        } else if (calendar.timeInMillis < lastTwoDayCalender.timeInMillis) {
                                            lastChatTime.set(chatTime)
                                        } else {
                                            lastChatTime.set(timeFormat.format(calendar.timeInMillis))
                                        }
                                        lastMessage.set(it.message)

                                        Log.e("it.message","it.message -> ${it.message}")
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    }
                    postReference.orderByKey().limitToLast(1).addValueEventListener(postListener)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setTyping(uid: String) {
        var chatId = ""
        val senderChatId = "${getFireBaseAuth().uid}_${uid}"
        val receiverChatId = "${uid}_${getFireBaseAuth().uid}"
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (ds in snapshot.children) {
                        if (ds.key != null) {
                            if (ds.key!! == senderChatId) {
                                chatId = senderChatId
                                break
                            } else if (ds.key!! == receiverChatId) {
                                chatId = receiverChatId
                                break
                            }
                        } else {
                            chatId = senderChatId
                            break
                        }
                    }
                    Log.e("PrintChatID","chatId is the  -> $chatId")
                    val reference = getDataBaseReference().child(AppConstant.TYPING_TABLE).child(chatId)
                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue<TypingModel>()?.let {
                                if (it.type) {
                                    lastMessage.set("Typing...")
                                }else {
                                    getLastChatAndTime(uid)
                                }
                                Log.e("PrintChatID","lastMessage is the  -> ${lastMessage.get()}")
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
}