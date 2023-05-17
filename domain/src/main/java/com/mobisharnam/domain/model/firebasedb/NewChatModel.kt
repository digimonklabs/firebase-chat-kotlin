package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class NewChatModel(
    val chatId: String,
    val senderID: String,
    val message: String,
    val dateTime: Long,
    val senderName: String,
    val receiverName: String,
    val receiverTyping: Boolean,
    val senderTyping: Boolean,
    val senderPendingMessage: Int,
    val receiverPendingMessage: Int
) : Parcelable {

    constructor() : this("", "", "",0, "", "",false,false,0,0)
}