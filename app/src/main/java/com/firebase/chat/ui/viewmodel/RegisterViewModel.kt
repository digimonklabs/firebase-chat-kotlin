package com.firebase.chat.ui.viewmodel

import android.util.Patterns
import androidx.databinding.ObservableField
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.interacter.RegisterUseCase

class RegisterViewModel(private val registerUseCase: RegisterUseCase): BaseViewModel(registerUseCase) {

    fun checkValidation(): Boolean {
        val email = registerEmail.get()
        val name = registerName.get()
        val password = registerPassword.get()

        if (email.isNullOrEmpty()) {
            registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.alert_enter_email))
            return false
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.alert_enter_valid_email))
            return false
        }else if (name.isNullOrEmpty()) {
            registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.alert_enter_name))
            return false
        }else if (password.isNullOrEmpty()) {
            registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.alert_enter_password))
            return false
        }else if (password.toString().length < 6) {
            registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.alert_enter_valid_password))
            return false
        }else {
            return true
        }
    }

    val registerEmail = ObservableField("")
    val registerName = ObservableField("")
    val registerPassword = ObservableField("")
}