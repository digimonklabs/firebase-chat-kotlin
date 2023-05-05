package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class User(
    val uid: String,
    val userName: String,
    val userEmail: String,
    val created: Long,
    val updated: Long,
    val online: Boolean,
    val lastMessage: String,
    val friendsList: Map<String,FriendsList> = emptyMap(),
    val token: String
) : Parcelable {

    constructor() : this( "","", "", 0,0, false,"",HashMap<String,FriendsList>(),"")
}