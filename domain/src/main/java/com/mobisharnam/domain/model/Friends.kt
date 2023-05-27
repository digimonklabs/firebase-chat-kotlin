package com.mobisharnam.domain.model

import android.os.Parcelable


@kotlinx.parcelize.Parcelize
class Friends(
    val friendUid: String,
    val name: String,
    val chatId: String,
    val typing: String,
    val typingId: String,
    val pendingCount: Int,
    val lastMessage: String,
    val dateTime: Long,
    val token: String,
    val notificationId: Int,
): Parcelable {
    constructor() : this( "", "", "", "", "",0, "", 0,"",0)
}