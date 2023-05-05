package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class ChatModel(
    val message: String,
    val chatId: String,
    val dateTime: Long,
    val read: Boolean,
    val profileImage: String
): Parcelable {
    constructor() : this( "", "", 0, false,"")
}