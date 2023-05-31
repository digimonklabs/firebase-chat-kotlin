package com.firebase.chat.ui.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.mobisharnam.domain.interacter.AddUserUseCase
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.response.Response
import kotlinx.coroutines.launch

class AddFriendsViewModel(private val addUserUseCase: AddUserUseCase) : BaseViewModel(addUserUseCase) {

    val noUser = ObservableBoolean(false)
    private val _friendsLiveData = MutableLiveData<Event<Response<ArrayList<NewUser>>>>()
    val friendsLiveData: LiveData<Event<Response<ArrayList<NewUser>>>>
        get() = _friendsLiveData

    fun getAllUser() {
        viewModelScope.launch {
            addUserUseCase.getAllUser().collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        response.data?.let { user ->
                            _friendsLiveData.postValue(Event(Response.success(user,0)))
                        }
                    }
                    Response.Status.ERROR -> {
                        _friendsLiveData.postValue(Event(Response.error(response.message.toString(),0)))
                    }
                    Response.Status.EXCEPTION -> {
                        _friendsLiveData.postValue(Event(Response.exception(response.message.toString(),0)))
                    }
                }
            }
        }
    }

    fun sendInvitation() {
        sendInvitationList.get()?.let { invitations ->
            viewModelScope.launch {
                addUserUseCase.sendInvitation(invitations)
            }
        }
    }
}