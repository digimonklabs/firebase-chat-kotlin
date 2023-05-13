package com.mobisharnam.domain.util

object AppConstant {

    // Adapter name
    const val INVITATION_ADAPTER = "invitationAdapter"
    const val ADD_FRIENDS_ADAPTER = "addFriendsAdapter"
    const val CHAT_ADAPTER = "chatAdapter"
    const val CHAT_DETAILS_ADAPTER_ADAPTER = "chatDetailsAdapter"

    const val APP_PACKAGE = "app_package"
    const val APP_UID = "app_uid"
    var isRead = false

    // Firebase Database
    const val USER_TABLE = "User"
    const val CHAT_TABLE = "Chat"
    const val TYPING_TABLE = "Typing"
    const val NOTIFICATION_TABLE = "NotificationTable"
    const val USER_ONLINE = "online"
    const val MESSAGE_READ = "read"
    const val TOKEN = "token"
    const val TYPE = "type"
    const val FRIENDS_LIST = "friendsList"
    const val RECEIVER_TYPING = "receiverTyping"
    const val SENDER_TYPING = "senderTyping"
    var currentUser = ""

    // Firebase auth
    const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"

    // Fcm
    var notificationID = 0
    const val SEND = "send"
    const val TO = "to"
    const val DATA = "data"
    const val TITLE = "title"
    const val BODY = "body"
    const val SENDER_ID = "senderId"
    const val RECEIVER_ID = "receiverId"
    const val CHAT_ID = "chatID"
    const val NOTIFICATION = "notification"
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val NOTIFICATION_ID = "notificationId"
    const val SERVER_KEY = "key=AAAA63pJUSA:APA91bGB95AcEe6hLN4Op3y3lTSc96_M31UKxFYz7Enn0WJEXrOah5CTr-Cf2xuhSWEWuV9lawS-2EmZdsOVsvOb4YSnT5ttJUh40rXc077JX-82cpoWUVOuiQA9KI7BIOrAUqif2pmo"
}