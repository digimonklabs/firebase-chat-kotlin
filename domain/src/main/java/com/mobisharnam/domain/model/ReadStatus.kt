package com.mobisharnam.domain.model

import android.os.Parcelable


@kotlinx.parcelize.Parcelize
class ReadStatus(
    val senderId: String,
    val read: Boolean
): Parcelable {
    constructor() : this( "",  true)
}