package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class NewUser(
    val uid: String,
    val userName: String,
    val userEmail: String,
    val created: Long,
    val updated: Long,
    val online: Boolean,
    val friendsList: ArrayList<String>,
    val invitationList: ArrayList<String>,
    val token: String,
    val notificationId: Int,
    val lastSeen: Long
) : Parcelable {

    constructor() : this( "","", "", 0,0, false, ArrayList(), ArrayList(),"",0,System.currentTimeMillis())
}