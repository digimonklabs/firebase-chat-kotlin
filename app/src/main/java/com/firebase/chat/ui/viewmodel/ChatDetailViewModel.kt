package com.firebase.chat.ui.viewmodel

import android.app.NotificationManager
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.google.firebase.messaging.FirebaseMessaging
import com.mobisharnam.data.source.remote.settings.Setting
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.model.firebasedb.ChatModel
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatDetailViewModel(private val chatDetailUseCase: ChatDetailUseCase) :
    BaseViewModel(chatDetailUseCase) {

    private val _chatLiveData = MutableLiveData<Event<Response<ArrayList<ChatModel>>>>()
    val chatLiveData: LiveData<Event<Response<ArrayList<ChatModel>>>>
        get() = _chatLiveData

    private val notificationId = ObservableField(0)
    private val receiverNotificationId = ObservableField(0)
    private val userToken = ObservableField("")
    private var chatId = ""
    val chatListModel = ObservableField(ArrayList<ChatModel>())
    val chatMessage = ObservableField("")
    val receiverName = ObservableField("")
    val userImageName = ObservableField("")
    val userStatus = ObservableField("")
    private val pendingMessageCount = ObservableField(0)
  //  var allowRead = false
    private val todayDateTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val yesterDayDateTime = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.ENGLISH)
    private val chatDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    private val chatDayDateFormat = SimpleDateFormat("d", Locale.ENGLISH)
    private val timer = object : CountDownTimer(3000L, 3000L) {
        override fun onTick(p0: Long) {

        }

        override fun onFinish() {
            setUserStatus(chatId)
        }
    }

    init {
        chatDetailUseCase.allowRead = true
    }

    override fun onCleared() {
        super.onCleared()

        chatDetailUseCase.allowRead = false
    }

    fun getChatDate(timeStamp: Long): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = timeStamp
        return chatDateFormat.format(calender.timeInMillis)
    }

    fun getDateTime(timeStamp: Long): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = timeStamp
        return timeFormat.format(calender.timeInMillis)
    }

    fun getChat(chatId: String) {
        viewModelScope.launch {
            chatDetailUseCase.getAllChat(chatId).collect {
                when(it.status) {
                    Response.Status.SUCCESS -> {
                        _chatLiveData.postValue(Event(Response.success(it.data,it.errorCode)))

                    }
                    Response.Status.ERROR -> {
                        _chatLiveData.postValue(Event(Response.error(it.message.toString(),it.errorCode)))

                    }
                    Response.Status.EXCEPTION -> {
                        _chatLiveData.postValue(Event(Response.exception(it.message.toString(),it.errorCode)))

                    }
                }
            }
        }
    }

    fun setTyping(isTyping: Boolean, chatId: String) {
        val id = chatId.split("_")
        val receiverId = if (id[0] == getFireBaseAuth().uid) id[1] else id[0]
        val typingId = if (id[1] == getFireBaseAuth().uid) id[1] else id[0]

        val friendReference =
            getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)

        viewModelScope.launch {
            chatDetailUseCase.setTyping(friendReference,isTyping, typingId, chatId)
        }
    }

    fun setTypingId(isTyping: Boolean, chatId: String) {
        val id = chatId.split("_")
        val receiverId = if (id[0] == getFireBaseAuth().uid) id[1] else id[0]
        val typingId = if (id[1] == getFireBaseAuth().uid) id[1] else id[0]

        val friendReference =
            getDataBaseReference().child(AppConstant.FRIEND_TABLE).child(receiverId)

        viewModelScope.launch {
            chatDetailUseCase.setTypingId(friendReference,isTyping, typingId, chatId)
        }
    }

    fun sendNotificationID(notificationId: Int, receiverId: String) {
        getDataBaseReference().child(AppConstant.NOTIFICATION_TABLE)
            .child("${receiverId}_${getFireBaseAuth().uid}").setValue(notificationId)
    }

    private fun getPendingMessage(chatId: String) {
        viewModelScope.launch {
            chatDetailUseCase.getPendingMessage(chatId).collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        var receiverCount = 0
                        var senderCount = 0
                        response.data?.forEach { chatModel ->
                            if (!chatModel.read) {
                                if (getFireBaseAuth().uid == chatModel.senderID) {
                                    senderCount++
                                } else {
                                    receiverCount++
                                }
                            }
                        }
                        pendingMessageCount.set(receiverCount)
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }
    }

    fun setSender(
        userName: String
    ) {
        receiverName.set(userName)
        try {
            val name = userName.split(" ")
            userImageName.set(name[0][0] + name[1][0].toString())
        } catch (e: Exception) {
            userImageName.set(userName.first().toString())
        }
    }

    fun sendMessage(
        chatId: String,
        chatItem: ChatModel?,
        token: String
    ) {
        val message = chatMessage.get()?.trim()
        val references =
            getDataBaseReference().child(AppConstant.CHAT_TABLE).child(chatId)

        val receiver = chatId.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) receiver[1] else receiver[0]

        chatItem?.let {
            if (chatDayDateFormat.format(it.dateTime)
                    .toInt() < chatDayDateFormat.format(System.currentTimeMillis()).toInt()
            ) {
                val emptyChat = ChatModel(
                    chatId = chatId,
                    message = "",
                    senderID = getFireBaseAuth().uid.toString(),
                    dateTime = System.currentTimeMillis(),
                    read = true,
                    status = 1
                )

                references.push().setValue(emptyChat).addOnCompleteListener {
                    Log.e("PrintChatchatItem", "chatItem Complete")
                }.addOnFailureListener {
                    Log.e("PrintChatchatItem", "chatItem fail")
                }
            }
        }
        val count = pendingMessageCount.get()?.plus(1)
        pendingMessageCount.set(0)
        pendingMessageCount.set(count)
        val chat = ChatModel(
            chatId = chatId,
            message = message.toString(),
            senderID = getFireBaseAuth().uid.toString(),
            dateTime = System.currentTimeMillis(),
            read = true,
            status = 1
        )

        chatMessage.set("")

        references.push().setValue(chat).addOnCompleteListener {
            Log.e("PrintAfterComplet","before addOnCompleteListener")
            viewModelScope.launch {
                chatDetailUseCase.updateMessageStatus(references)
                chatDetailUseCase.updateCurrentUserStatus(chatId,message)
                chatDetailUseCase.updateFriendStatus(chatId,message,receiverId)
            }

            val header = HashMap<String, String>()
            header[AppConstant.AUTHORIZATION] = AppConstant.SERVER_KEY
            header[AppConstant.CONTENT_TYPE] = AppConstant.APPLICATION_JSON
            Setting.HEADER = header
            chatDetailUseCase.getSendNotification(
                receiverId,
                message,
                chatId,
                notificationId.get() ?: 0,
                receiverNotificationId.get() ?: 0,
                token,
                userToken.get() ?: ""
            )

            Log.e("PrintReceiverNotification","send message -> ${notificationId.get()}")
        }.addOnFailureListener {
            Log.e("chatMessage", "chatMessage fail to send")
        }
    }

    fun setMarkAsRead(chatId: String) {
        viewModelScope.launch {
            chatDetailUseCase.setMarkAsRead(chatId).collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        getPendingMessage(chatId)
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }

        viewModelScope.launch {
            chatDetailUseCase.setChatRead(chatId)
        }
    }

    fun setTyping() {
        viewModelScope.launch {
            chatDetailUseCase.setTypingStatus().collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                       response.data?.let {
                           timer.cancel()
                           timer.start()
                           userStatus.set(
                               chatDetailUseCase.getContext().getString(R.string.alert_typing)
                           )
                       }
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }
    }

    fun setUserStatus(chatId: String) {
        this.chatId = chatId
        val bothId = chatId.split("_")
        val receiverId = if (bothId[0] == getFireBaseAuth().uid) bothId[1] else bothId[0]

        viewModelScope.launch {
            chatDetailUseCase.getFriendStatus(receiverId).collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        response.data?.let { user ->
                            receiverNotificationId.set(user.notificationId)
                            Log.e("PrintReceiverNotification","id -> ${user.notificationId}")
                            val calendar = Calendar.getInstance()
                            val lastSeenCalender = Calendar.getInstance()
                            val yesterDayCalender = Calendar.getInstance()
                            lastSeenCalender.timeInMillis = user.lastSeen
                            yesterDayCalender.add(Calendar.DATE, -1)

                            calendar[Calendar.HOUR_OF_DAY] = 0
                            calendar[Calendar.MINUTE] = 0
                            calendar[Calendar.SECOND] = 0
                            calendar[Calendar.MILLISECOND] = 0

                            if (user.online) {
                                userStatus.set(
                                    chatDetailUseCase.getContext().getString(R.string.online)
                                )
                            } else if (lastSeenCalender.timeInMillis > calendar.timeInMillis) {
                                val yesterDaySeen = todayDateTime.format(user.lastSeen)
                                userStatus.set(
                                    "${
                                        chatDetailUseCase.getContext()
                                            .getString(R.string.last_seen_today)
                                    } $yesterDaySeen"
                                )
                            } else if (lastSeenCalender.timeInMillis < calendar.timeInMillis) {
                                val lastSeen = todayDateTime.format(user.lastSeen)
                                userStatus.set(
                                    "${
                                        chatDetailUseCase.getContext()
                                            .getString(R.string.last_seen_yesterday)
                                    } $lastSeen"
                                )
                            } else if (lastSeenCalender.timeInMillis < yesterDayCalender.timeInMillis) {
                                val lastSeen = yesterDayDateTime.format(user.lastSeen)
                                userStatus.set(
                                    "${
                                        chatDetailUseCase.getContext().getString(R.string.last_seen)
                                    } $lastSeen"
                                )
                            }
                        }
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }

    }

    fun getNotificationId(chatId: String) {
        val receiver = chatId.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) receiver[1] else receiver[0]

        viewModelScope.launch {
            chatDetailUseCase.getReceiverNotificationId(receiverId).collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        val notificationManager = chatDetailUseCase.getContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        response.data?.let { notificationId ->
                            Log.e("getNotificationId","getNotificationId  $notificationId")
                            notificationManager.cancel(notificationId)
                        }
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }

            chatDetailUseCase.getSenderNotificationId().collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        response.data?.let { id ->
                            notificationId.set(id)
                        }
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }
    }

    fun getToken(chatId: String) {
        val receiver = chatId.split("_")
        val receiverId = if (receiver[0] == getFireBaseAuth().uid) receiver[1] else receiver[0]
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            userToken.set(it.result)
        }

        chatDetailUseCase.setFriendToken(receiverId)
    }
}