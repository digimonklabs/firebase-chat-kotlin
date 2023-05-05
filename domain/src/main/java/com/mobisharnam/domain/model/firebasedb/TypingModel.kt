package com.mobisharnam.domain.model.firebasedb

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class TypingModel(
    val type: Boolean,
    val uid: String,
): Parcelable {
    constructor() : this(false,"")
}