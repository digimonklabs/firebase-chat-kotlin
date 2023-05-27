package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.InvitationUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Random

class InvitationViewModel(private val invitationUseCase: InvitationUseCase) :
    BaseViewModel(invitationUseCase) {

    val noInvitation = ObservableBoolean(false)
    private val _invitationLiveData = MutableLiveData<Event<Response<ArrayList<Invitation>>>>()
    val invitationLiveData: LiveData<Event<Response<ArrayList<Invitation>>>>
        get() = _invitationLiveData

    fun getInvitation() {

        viewModelScope.launch {
            val users = invitationUseCase.getInvitation()
            users.collect {
                when(it.status) {
                    Response.Status.SUCCESS -> {
                        _invitationLiveData.postValue(Event(Response.success(it.data,it.errorCode)))
                    }
                    Response.Status.ERROR -> {
                        _invitationLiveData.postValue(Event(Response.exception(it.message.toString(),it.errorCode)))
                    }
                    Response.Status.EXCEPTION -> {
                        _invitationLiveData.postValue(Event(Response.exception(it.message.toString(),it.errorCode)))
                    }
                }
            }
        }
        /*val invitationReference = getDataBaseReference().child(AppConstant.INVITATION_TABLE)
            .child(getFireBaseAuth().uid.toString())
        invitationReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val invitation = ArrayList<Invitation>()
                for (sp in snapshot.children) {
                    sp.getValue<Invitation>()?.let {
                        invitation.add(it)
                    }
                }
                noInvitation.set(invitation.isEmpty())
                _invitationLiveData.postValue(Event(Response.success(invitation, 0)))

            }

            override fun onCancelled(error: DatabaseError) {
                _invitationLiveData.postValue(
                    Event(
                        Response.exception(
                            error.toException().message.toString(),
                            101
                        )
                    )
                )
            }
        })*/
    }

    fun acceptInvitation(uid: String, accept: Boolean) {
        if (accept) {
            getDataBaseReference().child(AppConstant.USER_TABLE)
                .child(getFireBaseAuth().uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friendsReference =
                            getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(uid).push()
                        snapshot.getValue<NewUser>()?.let {
                            val friend = Friends(
                                friendUid = it.uid,
                                name = it.userName,
                                chatId = "${getFireBaseAuth().uid}_$uid",
                                typing = "",
                                typingId = "",
                                pendingCount = 0,
                                lastMessage = "",
                                dateTime = System.currentTimeMillis(),
                                token = it.token,
                                notificationId = Random().nextInt(999999)
                            )
                            friendsReference.setValue(friend)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            getDataBaseReference().child(AppConstant.USER_TABLE).child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friendsReference =
                            getDataBaseReference().child(AppConstant.FRIEND_TABLE)
                                .child(getFireBaseAuth().uid.toString()).push()
                        snapshot.getValue<NewUser>()?.let {
                            val friend = Friends(
                                friendUid = it.uid,
                                name = it.userName,
                                chatId = "${getFireBaseAuth().uid}_$uid",
                                typing = "",
                                typingId = "",
                                pendingCount = 0,
                                lastMessage = "",
                                dateTime = System.currentTimeMillis(),
                                token = it.token,
                                notificationId = Random().nextInt(999999)
                            )
                            friendsReference.setValue(friend)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        val removeInvitationReference = getDataBaseReference().child(AppConstant.INVITATION_TABLE)
            .child(getFireBaseAuth().uid.toString())
        removeInvitationReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        sp.getValue(Invitation::class.java)?.let {
                            if (it.senderId == uid) {
                                removeInvitationReference.child(sp.key.toString()).removeValue()
                            }
                        }
                    }
                    getInvitation()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}