package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class ChatModel(
    val chatId: String,
    val senderID: String,
    val message: String,
    val dateTime: Long,
    val read: Boolean,
    val status: Int
) : Parcelable {

    constructor() : this("", "", "",0,false,1)
}