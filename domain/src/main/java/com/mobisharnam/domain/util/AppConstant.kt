package com.mobisharnam.domain.util

object AppConstant {

    const val APP_PACKAGE = "app_package"
    const val APP_UID = "app_uid"
    var isRead = false

    // Firebase Database
    const val USER_TABLE = "User"
    const val CHAT_TABLE = "Chat"
    const val TYPING_TABLE = "Typing"
    const val USER_FRIEND_LIST = "friendsList"
    const val USER_ONLINE = "online"
    const val MESSAGE_READ = "read"
    const val TOKEN = "token"
    const val TYPE = "type"
    const val FRIENDS_LIST = "friendsList"
    var currentUser = ""

    // Firebase auth
    const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"

    // Fcm
    var notificationID = 0
    const val SEND = "send"
    const val TO = "to"
    const val TITLE = "title"
    const val BODY = "body"
    const val SENDER_ID = "senderId"
    const val RECEIVER_ID = "receiverId"
    const val CHAT_ID = "chatID"
    const val NOTIFICATION = "notification"
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val SERVER_KEY = "key=AAAA63pJUSA:APA91bGB95AcEe6hLN4Op3y3lTSc96_M31UKxFYz7Enn0WJEXrOah5CTr-Cf2xuhSWEWuV9lawS-2EmZdsOVsvOb4YSnT5ttJUh40rXc077JX-82cpoWUVOuiQA9KI7BIOrAUqif2pmo"
}