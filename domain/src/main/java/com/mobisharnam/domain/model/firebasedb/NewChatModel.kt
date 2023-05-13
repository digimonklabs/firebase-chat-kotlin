package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class NewChatModel(
    val message: String,
    val senderID: String,
    val dateTime: Long,
    val read: Boolean,
    val senderTyping: Boolean,
    val receiverTyping: Boolean,
    val isChatOnline: Boolean,
    val senderName: String,
    val receiverName: String,
    val chatId: String,
    val senderPendingMessage: Int,
    val receiverPendingMessage: Int
): Parcelable {
    constructor() : this( "", "", 0, false,false,false,false,"","","",0,0)
}