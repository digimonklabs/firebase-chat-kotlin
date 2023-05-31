package com.mobisharnam.domain.interacter

import android.content.Context
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatListUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
): BaseUseCase(context,remoteRepository,localRepository) {
    suspend fun getFriends(): Flow<Response<ArrayList<Friends>>> = callbackFlow {
        val friendList = ArrayList<Friends>()
        val friendsReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
            .child(getFireBaseAuth().uid.toString())
        friendsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e("PrintChatListCall","PrintChatListCall done in onChildAdded")
                if (snapshot.exists()) {
                    snapshot.getValue<Friends>()?.let {
                        friendList.add(it)
                    }
                    friendList.sortByDescending {
                        it.dateTime
                    }
                    trySend(Response.success(friendList,0)).isSuccess
                }else {
                    trySend(Response.error(AppConstant.NO_FRIEND,0)).isSuccess
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e("PrintChatListCall","PrintChatListCall done in onChildChanged")
                snapshot.getValue<Friends>()?.let {
                    for (i in 0 until friendList.size) {
                        if (it.chatId == friendList[i].chatId) {
                            friendList.removeAt(i)
                            friendList.add(it)
                        }
                    }
                    friendList.sortByDescending {
                        it.dateTime
                    }
                    trySend(Response.success(friendList,0)).isSuccess
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Response.exception(error.message,201)).isSuccess
            }
        })

        awaitClose { }
    }
}