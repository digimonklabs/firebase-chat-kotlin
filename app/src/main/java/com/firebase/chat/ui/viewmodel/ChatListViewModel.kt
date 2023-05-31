package com.firebase.chat.ui.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.model.Friends
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.response.Response
import kotlinx.coroutines.launch

class ChatListViewModel(private val chatListUseCase: ChatListUseCase) :
    BaseViewModel(chatListUseCase) {

    var userList = ObservableField(ArrayList<NewUser>())

    private val _friendsLiveData = MutableLiveData<Event<Response<ArrayList<Friends>>>>()
    val friendsLiveData: LiveData<Event<Response<ArrayList<Friends>>>>
        get() = _friendsLiveData

    fun getFriends() {
        viewModelScope.launch {
            chatListUseCase.getFriends().collect {
                when (it.status) {
                    Response.Status.SUCCESS -> {
                        _friendsLiveData.postValue(Event(Response.success(it.data, it.errorCode)))
                    }

                    Response.Status.ERROR -> {
                        _friendsLiveData.postValue(
                            Event(
                                Response.error(
                                    it.message.toString(),
                                    it.errorCode
                                )
                            )
                        )
                    }

                    Response.Status.EXCEPTION -> {
                        _friendsLiveData.postValue(
                            Event(
                                Response.exception(
                                    it.message.toString(),
                                    it.errorCode
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}