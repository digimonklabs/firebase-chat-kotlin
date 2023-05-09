package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant
import java.util.Collections

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    val noFriend = ObservableBoolean(false)
    var userList = ObservableField(ArrayList<User>())
    var friendList = ObservableField(ArrayList<String>())
    var friendUid = ObservableField(ArrayList<String>())
    val latestTimeStamp = ArrayList<String>()

    fun init(onSetAdapter: OnSetAdapter) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    userList.get()?.clear()
                    friendList.get()?.clear()
                    for (sp in dataSnapshot.children) {
                        val user = sp.getValue(User::class.java)
                        user?.let {
                            if (sp.key == getFireBaseAuth().uid) {
                                it.friendsList.forEach {
                                    friendUid.get()?.add(it.key)
                                }

                                it.friendsList.keys.forEach {
                                    friendList.get()?.add(it)
                                }
                            } else {
                                userList.get()?.add(it)
                            }
                        }
                    }
                    friendUid.get()?.isEmpty()?.let { noFriend.set(it) }
                    onSetAdapter.onSetAdapter()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("postListener", "loadPost:onCancelled", databaseError.toException())
            }
        }
        val userRef = getDataBaseReference().child(AppConstant.USER_TABLE)
        userRef.addListenerForSingleValueEvent(postListener)
    }
}