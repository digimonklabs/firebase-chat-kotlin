package com.mobisharnam.domain.interacter

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobisharnam.domain.model.notification.NotificationResponse
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatDetailUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
): BaseUseCase(context,remoteRepository,localRepository) {

    suspend fun sendNotification(send: String, jsonObject: JSONObject) {
        val response = remoteRepository.post(NotificationResponse::class.java,send,jsonObject.toString())
        AppConstant.notificationID = response.data?.multicast_id?.toInt()  ?: 0
    }

    fun getSendNotification(receiverId: String, message: String?,chatId: String) {
        val reference = getDataBaseReference().child("User").child(receiverId).child("token")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(String::class.java)?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val jsonObject = JSONObject()
                        jsonObject.put(AppConstant.TO,it)
                        val notificationObject = JSONObject()
                        notificationObject.put(AppConstant.TITLE,AppConstant.currentUser)
                        notificationObject.put(AppConstant.BODY,message)
                        notificationObject.put(AppConstant.CHAT_ID,chatId)
                        notificationObject.put(AppConstant.RECEIVER_ID,receiverId)
                        notificationObject.put(AppConstant.SERVER_KEY,receiverId)
                        jsonObject.put(AppConstant.NOTIFICATION,notificationObject)
                        sendNotification(AppConstant.SEND,jsonObject)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}