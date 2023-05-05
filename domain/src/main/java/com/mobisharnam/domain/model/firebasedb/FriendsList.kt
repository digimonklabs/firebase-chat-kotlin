package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable
import java.nio.file.LinkOption

@kotlinx.parcelize.Parcelize
class FriendsList(
    val lastMessage: String,
    val dateTime: Long,
): Parcelable {

    constructor() : this("",0)
}