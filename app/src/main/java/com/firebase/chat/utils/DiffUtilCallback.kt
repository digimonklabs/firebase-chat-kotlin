package com.firebase.chat.utils

import androidx.recyclerview.widget.DiffUtil
import com.mobisharnam.domain.model.firebasedb.NewChatModel

class DiffUtilCallback(
    private val oldList: ArrayList<NewChatModel>,
    private val newList: ArrayList<NewChatModel>,
): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.javaClass == newItem.javaClass
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.hashCode() == newItem.hashCode()
    }
}