package com.mobisharnam.domain.util

object AppConstant {

    // Adapter name
    const val INVITATION_ADAPTER = "invitationAdapter"
    const val ADD_FRIENDS_ADAPTER = "addFriendsAdapter"
    const val CHAT_ADAPTER = "chatAdapter"
    const val CHILD = "child"
    const val NO_FRIEND = "NO_FRIEND"
    const val CHAT_DETAILS_ADAPTER_ADAPTER = "chatDetailsAdapter"

    const val APP_PACKAGE = "app_package"
    const val APP_UID = "app_uid"
    var isRead = false

    // Firebase Database
    const val USER_TABLE = "User"
    const val CHAT_TABLE = "Chat"
    const val FRIEND_TABLE = "Friend"
    const val INVITATION_TABLE = "Invitations"
    const val NOTIFICATION_TABLE = "NotificationTable"
    const val USER_ONLINE = "online"
    const val LAST_SEEN = "lastSeen"
    const val MESSAGE_READ = "read"
    const val TOKEN = "token"
    const val TYPING = "typing"
    const val TYPING_ID = "typingId"
    const val LAST_MESSAGE = "lastMessage"
    const val PENDING_COUNT = "pendingCount"
    const val DATE_TIME = "dateTime"
    const val STATUS = "status"
    var currentUser = ""

    // Firebase auth
    const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"

    // Fcm
    const val IS_FROM_CHAT = "isFromChat"
    var notificationID = 0
    const val SEND = "send"
    const val TO = "to"
    const val DATA = "data"
    const val TITLE = "title"
    const val BODY = "body"
    const val USER_TOKEN = "userToken"
    const val FRIEND_TOKEN = "friendToken"
    const val RECEIVER_ID = "receiverId"
    const val CHAT_ID = "chatID"
    const val NOTIFICATION = "notification"
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val NOTIFICATION_ID = "notificationId"
    const val RECEIVER_NOTIFICATION_ID = "notificationId"
    const val SERVER_KEY = "key=AAAA63pJUSA:APA91bGB95AcEe6hLN4Op3y3lTSc96_M31UKxFYz7Enn0WJEXrOah5CTr-Cf2xuhSWEWuV9lawS-2EmZdsOVsvOb4YSnT5ttJUh40rXc077JX-82cpoWUVOuiQA9KI7BIOrAUqif2pmo"
}