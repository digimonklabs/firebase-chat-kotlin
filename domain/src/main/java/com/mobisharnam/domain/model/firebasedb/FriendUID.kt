package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@kotlinx.parcelize.Parcelize
class FriendUID(
    val friends: ArrayList<FriendsList>
): Parcelable {
    constructor(): this(ArrayList())
}