package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.BaseUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant

class AddFriendsViewModel(private val baseUseCase: BaseUseCase) : BaseViewModel(baseUseCase) {

    val noUser = ObservableBoolean(false)
    private val _friendsLiveData = MutableLiveData<Event<Response<ArrayList<NewUser>>>>()
    val friendsLiveData: LiveData<Event<Response<ArrayList<NewUser>>>>
        get() = _friendsLiveData

    fun getAllUser() {
        val userReference = getDataBaseReference().child(AppConstant.USER_TABLE)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val existingFriendList = ArrayList<NewUser>()
                val userList = ArrayList<NewUser>()
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        if (sp.key == getFireBaseAuth().uid) {
                            sp.getValue(NewUser::class.java)?.let {
                                existFriendList.set(it.friendsList)
                                invitationList.set(it.invitationList)
                            }
                        } else {
                            sp.getValue(NewUser::class.java)?.let {
                                existingFriendList.add(it)
                                userChatList.get()?.add(it)
                            }
                        }
                    }

                    getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(getFireBaseAuth().uid.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                userList.clear()
                                for (i in 0 until existingFriendList.size) {
                                    val hasfriend = snapshot.children.any {
                                        it.getValue(Friends::class.java)?.friendUid == existingFriendList[i].uid
                                    }
                                    if (!hasfriend) {
                                        userList.add(existingFriendList[i])
                                    }
                                }
                                noUser.set(userList.isEmpty())
                                _friendsLiveData.postValue(Event(Response.success(userList, 0)))
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

    fun sendInvitation() {
        Log.e("sendInvitation", "sendInvitation -> ${sendInvitationList.get()?.size}")
        getDataBaseReference().child(AppConstant.USER_TABLE).child(getFireBaseAuth().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val senderName = snapshot.getValue(NewUser::class.java)?.userName
                    sendInvitationList.get()?.forEach { friendUid ->
                        val invitation = Invitation(
                            senderName = senderName ?: "",
                            senderId = getFireBaseAuth().uid.toString()
                        )
                        val invitationReference =
                            getDataBaseReference().child(AppConstant.INVITATION_TABLE).child(friendUid.senderId)
                        invitationReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (sp in snapshot.children) {
                                        sp.getValue<Invitation>()?.let {
                                            if (it.senderId != invitation.senderId) {
                                                invitationReference.push().setValue(invitation)
                                            }
                                        }
                                    }
                                } else {
                                    invitationReference.push().setValue(invitation)
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