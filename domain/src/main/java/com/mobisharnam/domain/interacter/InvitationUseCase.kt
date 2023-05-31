package com.mobisharnam.domain.interacter

import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Random

class InvitationUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
): BaseUseCase(context,remoteRepository,localRepository) {

    fun getInvitation(): Flow<Response<ArrayList<Invitation>>> = flow {
        val ref = getDataBaseReference().child(AppConstant.INVITATION_TABLE)
        try {
            val snapshot = ref.get().await()
            val userList = ArrayList<Invitation>()

            for (childSnapshot in snapshot.children) {
                try {
                    childSnapshot.getValue(Invitation::class.java)?.let {
                        userList.add(it)
                    }
                }catch (e: Exception) {
                    emit(Response.exception(e.message.toString(),101))
                }
            }
            userList.let {
                Log.e("PrintUSerSize","PrintUSerSize -> ${it.size}")
                emit(Response.success(it,0))
            }
        }catch (e: Exception) {
            emit(Response.exception(e.message.toString(),201))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun acceptInvitation(uid: String) {
        val currentUserReference = getDataBaseReference().child(AppConstant.USER_TABLE).child(getFireBaseAuth().uid.toString())
        val friendReference = getDataBaseReference().child(AppConstant.USER_TABLE).child(uid)

        val currentUserSnapshot = currentUserReference.get().await()
        val friendSnapshot = friendReference.get().await()

        currentUserSnapshot.getValue<NewUser>()?.let {
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
            val friendsReference =
                getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(uid).push()
            friendsReference.setValue(friend)
        }

        friendSnapshot.getValue<NewUser>()?.let {
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
            val friendsReference =
                getDataBaseReference().child(AppConstant.FRIEND_TABLE)
                    .child(getFireBaseAuth().uid.toString()).push()
            friendsReference.setValue(friend)
        }
    }

    suspend fun removeInvitation(uid: String): Flow<Response<ArrayList<Invitation>>> = flow {
        val invitationList = ArrayList<Invitation>()
        val removeInvitationReference = getDataBaseReference().child(AppConstant.INVITATION_TABLE)
            .child(getFireBaseAuth().uid.toString())
        val invitationSnapshot = removeInvitationReference.get().await()

        for (sp in invitationSnapshot.children) {
            sp.getValue(Invitation::class.java)?.let { invitation ->
                invitationList.add(invitation)
                if (invitation.senderId == uid) {
                    removeInvitationReference.child(sp.key.toString()).removeValue()
                }
            }
        }
        emit(Response.success(invitationList,0))
    }
}