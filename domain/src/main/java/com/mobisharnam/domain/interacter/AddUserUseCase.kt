package com.mobisharnam.domain.interacter

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AddUserUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
): BaseUseCase(context,remoteRepository,localRepository) {
    suspend fun getAllUser(): Flow<Response<ArrayList<NewUser>>> = flow {
        val userReference = getDataBaseReference().child(AppConstant.USER_TABLE)
        val friendReference =  getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(getFireBaseAuth().uid.toString())
        val existingFriendList = ArrayList<NewUser>()
        val userList = ArrayList<NewUser>()

        val userSnapshot = userReference.get().await()
        val friendSnapshot = friendReference.get().await()
        for (sp in userSnapshot.children) {
            sp.getValue<NewUser>()?.let { user ->
                if (sp.key != getFireBaseAuth().uid) {
                    existingFriendList.add(user)
                }
            }
        }

        userList.clear()
        for (i in 0 until existingFriendList.size) {
            val hasfriend = friendSnapshot.children.any { snap ->
                snap.getValue(Friends::class.java)?.friendUid == existingFriendList[i].uid
            }
            if (!hasfriend) {
                userList.add(existingFriendList[i])
            }
        }
        emit(Response.success(userList,0))
    }

    fun sendInvitation(invitations: java.util.ArrayList<Invitation>) {
        getDataBaseReference().child(AppConstant.USER_TABLE).child(getFireBaseAuth().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val senderName = snapshot.getValue(NewUser::class.java)?.userName
                    invitations.forEach { friendInvitation ->
                        val invitation = Invitation(
                            senderName = senderName ?: "",
                            senderId = getFireBaseAuth().uid.toString()
                        )
                        val invitationReference =
                            getDataBaseReference().child(AppConstant.INVITATION_TABLE).child(friendInvitation.senderId)
                        invitationReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (sp in snapshot.children) {
                                        sp.getValue<Invitation>()?.let { invite ->
                                            if (invite.senderId != invitation.senderId) {
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