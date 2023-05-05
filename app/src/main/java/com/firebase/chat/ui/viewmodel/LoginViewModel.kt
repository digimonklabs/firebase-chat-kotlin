package com.firebase.chat.ui.viewmodel

import androidx.databinding.ObservableField
import com.firebase.chat.base.BaseViewModel
import com.mobisharnam.domain.interacter.LoginUseCase

class LoginViewModel(private val loginUseCase: LoginUseCase): BaseViewModel(loginUseCase) {

    val loginEmail = ObservableField("")
    val loginPassword = ObservableField("")
}