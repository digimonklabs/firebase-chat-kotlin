package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.callback.OnSetAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.interacter.InvitationUseCase
import com.mobisharnam.domain.model.firebasedb.NewChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant

class InvitationViewModel(private val invitationUseCase: InvitationUseCase) :
    BaseViewModel(invitationUseCase) {

    val invitations = ObservableField(ArrayList<NewUser>())
    fun getInvitation(onSetAdapter: OnSetAdapter) {
        val userReference = getDataBaseReference().child("Invitations")
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        sp.key?.let {
                            val splitId = it.split("_")
                            if (splitId[1] == getFireBaseAuth().uid) {
                                getDataBaseReference().child("USerTable").child(splitId[0])
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            snapshot.getValue<NewUser>()?.let {
                                                invitations.get()?.add(it)
                                            }
                                            Log.e(
                                                "Invitation",
                                                "onSetAdapter  -> ${invitations.get()?.size}"
                                            )
                                            onSetAdapter.onSetAdapter(AppConstant.INVITATION_ADAPTER)
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                            }
                        }
                    }
                } else {
                    invitations.set(ArrayList())
                    Log.e("Invitation", "not exist  -> ${invitations.get()?.size}")
                    onSetAdapter.onSetAdapter(AppConstant.INVITATION_ADAPTER)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun acceptInvitation(uid: String, accept: Boolean) {
        if (accept) {

            val chatId = "${getFireBaseAuth().uid}_$uid"
            val chat = NewChatModel(
                message = "",
                senderID = getFireBaseAuth().uid.toString(),
                dateTime = System.currentTimeMillis(),
                read = true,
                senderTyping = false,
                receiverTyping = false,
                isChatOnline = false,
                senderName = "",
                receiverName = "",
                chatId = chatId,
                senderPendingMessage= 0,
                receiverPendingMessage= 0
            )
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId).push().setValue(chat)
            getDataBaseReference().child("USerTable").child(getFireBaseAuth().uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(NewUser::class.java)?.let { user ->
                            val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
                            chatReference.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (sp in snapshot.children) {
                                        sp.getValue(NewChatModel::class.java)?.let {
                                            chatReference.child(sp.key.toString()).child("senderName").setValue(user.userName)
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
            getDataBaseReference().child("USerTable").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(NewUser::class.java)?.let { user ->
                            val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
                            chatReference.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (sp in snapshot.children) {
                                        sp.getValue(NewChatModel::class.java)?.let {
                                            chatReference.child(sp.key.toString()).child("receiverName").setValue(user.userName)
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

            getDataBaseReference().child("USerTable").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue<NewUser>()?.let {
                            val friends = ArrayList<String>()
                            friends.add(uid)
                            it.friendsList.forEach {
                                if (it != getFireBaseAuth().uid) {
                                    friends.add(it)
                                }
                            }
                            Log.e("PrintFriendsSize", " in uid  ${it.friendsList.size}")
                            getDataBaseReference().child("USerTable")
                                .child(getFireBaseAuth().uid.toString())
                                .child(AppConstant.FRIENDS_LIST).setValue(friends)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            getDataBaseReference().child("USerTable").child(getFireBaseAuth().uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue<NewUser>()?.let {
                            val friends = ArrayList<String>()
                            friends.add(getFireBaseAuth().uid.toString())
                            it.friendsList.forEach {
                                if (it != uid) {
                                    friends.add(it)
                                }
                            }
                            Log.e("PrintFriendsSize", " in firebase uid  ${it.friendsList.size}")
                            getDataBaseReference().child("USerTable").child(uid)
                                .child(AppConstant.FRIENDS_LIST).setValue(friends)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        getDataBaseReference().child("Invitations")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (sp in snapshot.children) {
                            sp.key?.let {
                                if (it.contains(uid)) {
                                    getDataBaseReference().child("Invitations").child(it)
                                        .removeValue()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}