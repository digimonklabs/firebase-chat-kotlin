package com.mobisharnam.domain.interacter

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class ChatDetailUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) : BaseUseCase(context, remoteRepository, localRepository) {

    var allowRead = false
    fun getSendNotification(
        receiverId: String,
        message: String?,
        chatId: String,
        notificationId: Int,
        receiverNotificationId: Int,
        token: String,
        userToken: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.e("PrintTOkenMessage","current token   ->  ${userToken}  $notificationId  $receiverNotificationId")
            Log.e("PrintTOkenMessage","friend token   ->  ${token}")
            val jsonObject = JSONObject()
            jsonObject.put(AppConstant.TO, token)
            val notificationObject = JSONObject()
            notificationObject.put(AppConstant.TITLE, AppConstant.currentUser)
            notificationObject.put(AppConstant.BODY, message)
            val dataObject = JSONObject()
            dataObject.put(AppConstant.NOTIFICATION_ID, receiverNotificationId)
            dataObject.put(AppConstant.RECEIVER_NOTIFICATION_ID, notificationId)
            dataObject.put(AppConstant.CHAT_ID, chatId)
            dataObject.put(AppConstant.RECEIVER_ID, receiverId)
            dataObject.put(AppConstant.USER_TOKEN, userToken)
            dataObject.put(AppConstant.FRIEND_TOKEN, token)
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
                    sp.getValue<ChatModel>()?.let { chatModel ->
                        Log.e("PrintCalledMethod","getAllChat")
                        chatList.add(chatModel)
                    }
                }
                trySend(Response.success(chatList, 0)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Response.exception(error.message, 0)).isSuccess
            }
        })

       awaitClose { }
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
             childSnapshot.getValue(Friends::class.java)?.let { friend ->
                 friendList.add(friend)
                 if (friend.chatId == chatId) {
                     Log.e("PrintCalledMethod","setTyping")
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
             childSnapshot.getValue(Friends::class.java)?.let { friend ->
                 friendList.add(friend)
                 if (friend.chatId == chatId) {
                     Log.e("PrintCalledMethod","setTypingId")
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
                                             Log.e("PrintCalledMethod","setFriendToken")
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
         snapshot.getValue<Int>()?.let { id ->
             Log.e("PrintCalledMethod","getReceiverNotificationId")
             emit(Response.success(id, 101))
         }
     }

     fun getSenderNotificationId(): Flow<Response<Int>> = flow {
         val getNotificationReference = getDataBaseReference().child(AppConstant.USER_TABLE)
             .child(getFireBaseAuth().uid.toString())
             .child(AppConstant.NOTIFICATION_ID)
         val snapshot = getNotificationReference.get().await()
         snapshot.getValue<Int>()?.let { id ->
             Log.e("PrintCalledMethod","getSenderNotificationId")
             emit(Response.success(id, 101))
         }
     }

     suspend fun getFriendStatus(receiverId: String): Flow<Response<NewUser>> = callbackFlow {
         getDataBaseReference().child(AppConstant.USER_TABLE).child(receiverId)
             .addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     snapshot.getValue<NewUser>()?.let { user ->
                         Log.e("PrintCalledMethod","getFriendStatus")
                         trySend(Response.success(user, 0))
                     }
                 }

                 override fun onCancelled(error: DatabaseError) {
                     trySend(Response.exception(error.message, 0))
                 }
             })
         awaitClose {  }
     }

     suspend fun setTypingStatus(): Flow<Response<Friends>> = callbackFlow {
         val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
             .child(getFireBaseAuth().uid.toString())

         friendReference.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 for (sp in snapshot.children) {
                     sp.getValue<Friends>()?.let { friends ->
                         if (friends.typingId != getFireBaseAuth().uid && friends.typingId.isNotEmpty()) {
                             Log.e("PrintCalledMethod","setTypingStatus")
                             trySend(Response.success(friends,0))
                         }
                     }
                 }
             }

             override fun onCancelled(error: DatabaseError) {
                 trySend(Response.exception(error.message,0))
             }
         })
         awaitClose {  }
     }

    suspend fun setMarkAsRead(chatId: String): Flow<Response<ArrayList<Friends>>> = callbackFlow {
        Log.e("PrintCalledMethodallowRead","allowRead -> $allowRead")
        val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
            .child(getFireBaseAuth().uid.toString())
         friendReference.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 val friendList = ArrayList<Friends>()
                 for (sp in snapshot.children) {
                     sp.getValue<Friends>()?.let { friend ->
                         if (friend.chatId == chatId && allowRead) {
                             friendReference.child(sp.key.toString())
                                 .child(AppConstant.PENDING_COUNT)
                                 .setValue(0)
                         }
                     }
                 }
                 trySend(Response.success(friendList, 0)).isSuccess
             }

             override fun onCancelled(error: DatabaseError) {
                 trySend(Response.exception(error.message,101)).isSuccess
             }
         })

        awaitClose {  }
     }

     fun setChatRead(chatId: String) {
         val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
         chatReference.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 for (sp in snapshot.children) {
                     sp.getValue<ChatModel>()?.let { chatModel ->
                         if (chatModel.senderID != getFireBaseAuth().uid && this@ChatDetailUseCase.allowRead) {
                             chatReference.child(sp.key.toString()).child(AppConstant.STATUS).setValue(3)
                         }
                     }
                 }
             }

             override fun onCancelled(error: DatabaseError) {

             }
         })
     }

     suspend fun getPendingMessage(chatId: String): Flow<Response<ArrayList<ChatModel>>> = flow {
         val chatReference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
         val snapshot = chatReference.get().await()
         val chatList = ArrayList<ChatModel>()
         for (sp in snapshot.children) {
             sp.getValue<ChatModel>()?.let { chat ->
                 Log.e("PrintCalledMethod","getPendingMessage")
                 chatList.add(chat)
             }
         }
         emit(Response.success(chatList,0))
     }

     suspend fun updateMessageStatus(references: DatabaseReference) {
         val snapshot = references.get().await()
         for (sp in snapshot.children) {
             sp.getValue<ChatModel>()?.let { chat ->
                 if (chat.status == 1) {
                     Log.e("PrintCalledMethod","updateMessageStatus")
                     references.child(sp.key.toString()).child(AppConstant.STATUS).setValue(2)
                 }
             }
         }
     }

     fun updateCurrentUserStatus(chatId: String, message: String?) {
         val friendReference = getDataBaseReference().child(AppConstant.FRIEND_TABLE)
             .child(getFireBaseAuth().uid.toString())
         friendReference.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 for (sp in snapshot.children) {
                     sp.getValue<Friends>()?.let {
                         if (it.chatId == chatId) {
                             Log.e("PrintCalledMethod","updateCurrentUserStatus")
                             friendReference.child(sp.key.toString()).child(AppConstant.LAST_MESSAGE)
                                 .setValue(message)
                             friendReference.child(sp.key.toString()).child(AppConstant.TYPING)
                                 .setValue("")
                             friendReference.child(sp.key.toString()).child(AppConstant.PENDING_COUNT)
                                 .setValue(0)
                             friendReference.child(sp.key.toString()).child(AppConstant.DATE_TIME)
                                 .setValue(System.currentTimeMillis())
                         }
                     }
                 }
             }

             override fun onCancelled(error: DatabaseError) {

             }
         })
     }

     fun updateFriendStatus(chatId: String, message: String?,receiverId: String) {
         val receiverReferenceReference =
             getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)
         receiverReferenceReference.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 for (sp in snapshot.children) {
                     sp.getValue<Friends>()?.let { friend ->
                         if (friend.chatId == chatId) {
                             Log.e("PrintCalledMethod","updateFriendStatus $receiverId")
                             receiverReferenceReference.child(sp.key.toString())
                                 .child(AppConstant.LAST_MESSAGE).setValue(message)
                             receiverReferenceReference.child(sp.key.toString()).child(AppConstant.TYPING)
                                 .setValue("")
                             receiverReferenceReference.child(sp.key.toString())
                                 .child(AppConstant.PENDING_COUNT).setValue(friend.pendingCount + 1)
                             receiverReferenceReference.child(sp.key.toString())
                                 .child(AppConstant.DATE_TIME).setValue(System.currentTimeMillis())
                         }
                     }
                 }
             }

             override fun onCancelled(error: DatabaseError) {

             }
         })
     }

    suspend fun setMessageStatus(chatId: String) {
        Log.e("setMessageStatus","setMessageStatus called done")
        val reference = getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)
        val snapshot = reference.get().await()

        for (sp in snapshot.children) {
            sp.getValue(ChatModel::class.java)?.let {
                if (it.senderID != getFireBaseAuth().uid) {
                    reference.child(sp.key.toString()).child(AppConstant.MESSAGE_READ).setValue(true)
                    reference.child(sp.key.toString()).child(AppConstant.STATUS).setValue(3)
                }
            }
        }
    }
}