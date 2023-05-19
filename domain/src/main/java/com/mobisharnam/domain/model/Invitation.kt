package com.mobisharnam.domain.model

import android.os.Parcelable


@kotlinx.parcelize.Parcelize
class Invitation(
    val senderId: String,
    val senderName: String
): Parcelable {
    constructor() : this( "",  "")
}