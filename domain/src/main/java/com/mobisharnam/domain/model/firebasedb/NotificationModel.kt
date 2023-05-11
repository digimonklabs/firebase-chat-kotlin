package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class NotificationModel(
    val notificationID: Int,
    val uid: String,
): Parcelable {
    constructor() : this(0,"")
}