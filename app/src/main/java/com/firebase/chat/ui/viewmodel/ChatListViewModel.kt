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
import com.mobisharnam.domain.model.firebasedb.FriendsList
import com.mobisharnam.domain.model.firebasedb.UidList
import com.mobisharnam.domain.model.firebasedb.User
import com.mobisharnam.domain.util.AppConstant
import java.util.Collections

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    val noFriend = ObservableBoolean(false)
    var userList = ObservableField(ArrayList<User>())
    var friendList = ObservableField(ArrayList<String>())
    var friendUid = ObservableField(ArrayList<UidList>())

    fun init(onSetAdapter: OnSetAdapter) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    userList.get()?.clear()
                    friendUid.get()?.clear()
                    friendList.get()?.clear()
                    for (sp in dataSnapshot.children) {
                        val user = sp.getValue(User::class.java)
                        user?.let {
                            if (sp.key == getFireBaseAuth().uid) {
                                it.friendsList.forEach {
                                    val uidList = UidList(it.key,it.value.dateTime)
                                    friendUid.get()?.add(uidList)
                                }

                                it.friendsList.keys.forEach {
                                    friendList.get()?.add(it)
                                }
                            } else {
                                userList.get()?.add(it)
                            }
                        }
                    }
                    val user: ArrayList<User> = userList.get()!!

                    friendUid.get()?.sortByDescending  {
                        it.dateTime
                    }

                    friendUid.get()?.forEach {
                        Log.e("PrintFriendUid","PrintFriendUid -> ${it.uid} -- ${it.dateTime}")
                    }
                    Log.e("PrintFriendUid","PrintFriendUid -> ------------------------")

                    userList.set(ArrayList())

                    Log.e("PrintUIdArray","user -> ${user?.size}  friend -> ${friendUid.get()?.size}")
                    for (i in 0 until friendUid.get()!!.size) {

                        for (j in 0 until user!!.size) {

                            if (user[j].uid == friendUid.get()!![i].uid) {
                                Log.e("PrintUIdArray","user -> ${user[j].uid}  friend -> ${friendUid.get()!![i].uid}")
                                userList.get()?.add(user[j])
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