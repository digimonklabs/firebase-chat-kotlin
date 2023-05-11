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
    val conversion: ArrayList<String>,
    var friendsList: Map<String,FriendsList> = emptyMap(),
    val token: String,
    val notificationId: Int
) : Parcelable {

    constructor() : this( "","", "", 0,0, false,"",ArrayList(),HashMap<String,FriendsList>(),"",0)
}