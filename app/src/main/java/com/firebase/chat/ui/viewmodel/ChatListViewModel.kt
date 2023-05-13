package com.firebase.chat.ui.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.model.firebasedb.FriendsList
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.model.firebasedb.UidList
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    val chatList = ObservableField(ArrayList<NewChatModel>())
//    val noFriend = ObservableBoolean(false)
    var userList = ObservableField(ArrayList<User>())
    var friendList = ObservableField(ArrayList<String>())
    var addFriend = ObservableField(HashMap<String,FriendsList>())
    var friendUid = ObservableField(ArrayList<UidList>())

    fun initUserChat(onSetAdapter: OnSetAdapter) {
        val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE)
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val chat = ArrayList<NewChatModel>()
                for (ds in snapshot.children) {
                    if (ds.key!!.contains(getFireBaseAuth().uid.toString())) {
                        chatReference.child(ds.key.toString()).orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (sp in snapshot.children) {
                                    sp.getValue(NewChatModel::class.java)?.let {
                                        chat.add(it)
                                    }
                                }
                                chatList.set(ArrayList())
                                chatList.set(chat)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    onSetAdapter.onSetAdapter(AppConstant.CHAT_ADAPTER)
                                },500)
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun sendInvitation() {
        existFriendList.get()?.forEach { friendUid ->
            val invitationId = "${getFireBaseAuth().uid}_${friendUid}"
            getDataBaseReference().child("Invitations").child(invitationId).setValue(true)
        }
    }
}