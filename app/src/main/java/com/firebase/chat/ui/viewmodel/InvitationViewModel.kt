package com.firebase.chat.ui.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Event
import com.mobisharnam.domain.interacter.InvitationUseCase
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.response.Response
import kotlinx.coroutines.launch

class InvitationViewModel(private val invitationUseCase: InvitationUseCase) :
    BaseViewModel(invitationUseCase) {

    val noInvitation = ObservableBoolean(false)
    private val _invitationLiveData = MutableLiveData<Event<Response<ArrayList<Invitation>>>>()
    val invitationLiveData: LiveData<Event<Response<ArrayList<Invitation>>>>
        get() = _invitationLiveData

    fun getInvitation() {

        viewModelScope.launch {
            invitationUseCase.getInvitation().collect {
                when(it.status) {
                    Response.Status.SUCCESS -> {
                        _invitationLiveData.postValue(Event(Response.success(it.data,it.errorCode)))
                    }
                    Response.Status.ERROR -> {
                        _invitationLiveData.postValue(Event(Response.exception(it.message.toString(),it.errorCode)))
                    }
                    Response.Status.EXCEPTION -> {
                        _invitationLiveData.postValue(Event(Response.exception(it.message.toString(),it.errorCode)))
                    }
                }
            }
        }
    }

    fun acceptInvitation(uid: String, accept: Boolean) {
        if (accept) {
            viewModelScope.launch {
                invitationUseCase.acceptInvitation(uid)
            }
        }

        viewModelScope.launch {
            invitationUseCase.removeInvitation(uid).collect { response ->
                when(response.status) {
                    Response.Status.SUCCESS -> {
                        getInvitation()
                    }
                    Response.Status.ERROR -> {

                    }
                    Response.Status.EXCEPTION -> {

                    }
                }
            }
        }
    }
}