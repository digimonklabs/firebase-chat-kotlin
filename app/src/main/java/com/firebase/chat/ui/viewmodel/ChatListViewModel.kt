package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnAdapterChange
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    val chatList = ObservableField(ArrayList<NewChatModel>())

    //    val noFriend = ObservableBoolean(false)
    var userList = ObservableField(ArrayList<NewUser>())
    var friendList = ArrayList<Friends>()

    fun getLatestMessage(chatId: ArrayList<String>, onAdapterChange: OnAdapterChange, isSingleCall: Boolean) {
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        val chat = ArrayList<NewChatModel>()
        if (isSingleCall) {
            chatId.forEach {
                chatReference.child(it).orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (sp in snapshot.children) {
                            sp.getValue(NewChatModel::class.java)?.let {
                                chat.add(it)
                            }
                        }
                        chatList.set(ArrayList())
                        chatList.set(chat)
                        val chatArray = chatList.get()
                        chatArray?.sortByDescending {
                            it.dateTime
                        }
                        chatList.set(chatArray)
                        onAdapterChange.onAdapterChange(-1,-1,-1)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        } else {
            chatId.forEach {
                var coun = 0
                var snapKey = ""
                chatReference.child(it).orderByKey().limitToLast(1)
                    .addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val chatArray = chatList.get()
                            chatArray?.sortByDescending {
                                it.dateTime
                            }
                            var isNew = false
                            val message = chatList.get()!![chatList.get()!!.size - 1].message
                            chatList.set(chatArray)
                            var position = -1

                            snapshot.getValue(NewChatModel::class.java)?.let {
                                for (i in 0 until chatList.get()!!.size) {
                                    if (chatList.get()!![i].chatId == snapshot.ref.parent?.key) {
//                                        isNew = message != it.message
//                                        isNew = snapshot.ref.parent?.key == it.chatId
                                        isNew = snapKey != snapshot.key

                                        coun++

                                        Log.e(
                                            "ChildEventListener",
                                            "onChildAdded $coun $message  ${it.message} ${snapshot.key} -- ${snapshot.key} ${it.chatId}"
                                        )
                                        snapKey = snapshot.key.toString()
                                        chatList.get()?.removeAt(i)
                                        chatList.get()?.add(i,it)
                                        position = i
                                    }
                                }
                            }


                            if (isNew) {
                                Log.e("onAdapterChange","onAdapterChange called")
                                onAdapterChange.onAdapterChange(-1,0,position)
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                            var position = -1
                            snapshot.getValue(NewChatModel::class.java)?.let {
                                for (i in 0 until chatList.get()!!.size) {
                                    if (chatList.get()!![i].chatId == snapshot.ref.parent?.key) {
                                        chatList.get()?.removeAt(i)
                                        chatList.get()?.add(i,it)
                                        position = i
                                    }
                                }
                            }
                            Log.e("ChildEventListener", "onChildChanged position -> $position  ${snapshot.ref.parent?.key}")
                            onAdapterChange.onAdapterChange(position,0,0)
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {

                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }
        }
    }

    fun getFriends(onSetAdapter: OnSetAdapter) {
        val friendsReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(getFireBaseAuth().uid.toString())
        friendsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (!snapshot.exists()) {
                    noFriend.set(true)
                }
                snapshot.getValue<Friends>()?.let {
                    friendList.add(it)
                }
                friendList.sortByDescending {
                    it.dateTime
                }
                noFriend.set(friendList.isEmpty())
                onSetAdapter.onSetAdapter("")
                Log.e("getFriends","onChildAdded -> ${friendList.size}")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e("getFriends"," onChildChanged size -> ${friendList.size} ${snapshot.key} previousChildName -> $previousChildName")
                snapshot.getValue<Friends>()?.let {
                    Log.e("PrintSnapShortSize","size -> ${friendList.size}")
                    for (i in 0 until friendList.size) {
                        if (it.chatId == friendList[i].chatId) {
                            friendList.removeAt(i)
                            friendList.add(it)
                        }
                    }
                }
                Log.e("getFriends","getFriends onChildChanged after ${friendList.size}")
                friendList.sortByDescending {
                    it.dateTime
                }
                onSetAdapter.onSetAdapter("")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}