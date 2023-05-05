package com.firebase.chat.callback


interface OnSetPendingMessageCount {
    fun pendingMessageCount(count: Int)
}