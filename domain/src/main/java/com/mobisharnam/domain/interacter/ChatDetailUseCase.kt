package com.mobisharnam.domain.interacter

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.model.notification.NotificationResponse
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class ChatDetailUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) : BaseUseCase(context, remoteRepository, localRepository) {

    fun getSendNotification(
        receiverId: String,
        message: String?,
        chatId: String,
        notificationId: Int,
        token: String,
        userToken: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject()
            jsonObject.put(AppConstant.TO, token)
            val notificationObject = JSONObject()
            notificationObject.put(AppConstant.TITLE, AppConstant.currentUser)
            notificationObject.put(AppConstant.BODY, message)
            val dataObject = JSONObject()
            dataObject.put(AppConstant.NOTIFICATION_ID, notificationId)
            dataObject.put(AppConstant.CHAT_ID, chatId)
            dataObject.put(AppConstant.RECEIVER_ID, receiverId)
            dataObject.put(AppConstant.SERVER_KEY, receiverId)
            dataObject.put(AppConstant.SERVER_KEY, userToken)
            jsonObject.put(AppConstant.NOTIFICATION, notificationObject)
            jsonObject.put(AppConstant.DATA, dataObject)
            remoteRepository.post(
                NotificationResponse::class.java,
                AppConstant.SEND,
                jsonObject.toString()
            )
        }
    }

    suspend fun getAllChat(chatId: String): Flow<Response<ArrayList<ChatModel>>> = callbackFlow {
        val ref = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = ArrayList<ChatModel>()
                for (sp in snapshot.children) {
                    sp.getValue<ChatModel>()?.let {
                        chatList.add(it)
                    }
                }
                trySend(Response.success(chatList, 0)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Response.exception(error.message, 0)).isSuccess
            }
        })

        awaitClose {

        }
    }

    suspend fun setTyping(
        friendReference: DatabaseReference,
        isTyping: Boolean,
        typingId: String,
        chatId: String
    ) {
        val snapshot = friendReference.get().await()
        val friendList = ArrayList<Friends>()
        for (childSnapshot in snapshot.children) {
            childSnapshot.getValue(Friends::class.java)?.let {
                friendList.add(it)
                if (it.chatId == chatId) {
                    if (isTyping) {
                        friendReference.child(childSnapshot.key.toString())
                            .child(AppConstant.TYPING).setValue(typingId)
                    } else {
                        friendReference.child(childSnapshot.key.toString())
                            .child(AppConstant.TYPING).setValue("")
                    }
                }
            }
        }
    }

    suspend fun setTypingId(
        friendReference: DatabaseReference,
        isTyping: Boolean,
        typingId: String,
        chatId: String
    ) {
        val snapshot = friendReference.get().await()
        val friendList = ArrayList<Friends>()
        for (childSnapshot in snapshot.children) {
            childSnapshot.getValue(Friends::class.java)?.let {
                friendList.add(it)
                if (it.chatId == chatId) {
                    if (isTyping) {
                        friendReference.child(childSnapshot.key.toString())
                            .child(AppConstant.TYPING_ID)
                            .setValue(typingId)
                    } else {
                        friendReference.child(childSnapshot.key.toString())
                            .child(AppConstant.TYPING_ID)
                            .setValue("")
                    }
                }
            }
        }
    }

    fun setFriendToken(receiverId: String) {
        getDataBaseReference().child(AppConstant.USER_TABLE).child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<NewUser>()?.let { user ->
                        val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
                            .child(getFireBaseAuth().uid.toString())
                        friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (sp in snapshot.children) {
                                    sp.getValue<Friends>()?.let { friend ->
                                        if (friend.friendUid == receiverId && friend.token != user.token) {
                                            friendReference.child(sp.key.toString())
                                                .child(AppConstant.TOKEN).setValue(user.token)
                                        }
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
    }

    suspend fun getReceiverNotificationId(receiverId: String): Flow<Response<Int>> = flow {
        val clearNotificationReference =
            getDataBaseReference().child(AppConstant.USER_TABLE).child(receiverId)
                .child(AppConstant.NOTIFICATION_ID)
        val snapshot = clearNotificationReference.get().await()
        snapshot.getValue<Int>()?.let {
            emit(Response.success(it, 101))
        }
    }.catch {
        emit(Response.exception(it.message.toString(), 201))
    }

    fun getSenderNotificationId(): Flow<Response<Int>> = flow {
        val getNotificationReference = getDataBaseReference().child(AppConstant.USER_TABLE)
            .child(getFireBaseAuth().uid.toString())
            .child(AppConstant.NOTIFICATION_ID)
        val snapshot = getNotificationReference.get().await()
        snapshot.getValue<Int>()?.let {
            emit(Response.success(it, 101))
        }
    }.catch {
        emit(Response.exception(it.message.toString(), 201))
    }

    suspend fun getFriendStatus(receiverId: String): Flow<Response<NewUser>> = callbackFlow {
        getDataBaseReference().child(AppConstant.USER_TABLE).child(receiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<NewUser>()?.let {
                        trySend(Response.success(it, 0))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Response.exception(error.message, 0))
                }
            })
    }.catch {
        emit(Response.exception(it.message.toString(), 0))
    }

    suspend fun setTypingStatus(): Flow<Response<Friends>> = callbackFlow {
        val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
            .child(getFireBaseAuth().uid.toString())

        friendReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    sp.getValue<Friends>()?.let { friends ->
                        if (friends.typingId != getFireBaseAuth().uid && friends.typingId.isNotEmpty()) {
                            trySend(Response.success(friends,0))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}