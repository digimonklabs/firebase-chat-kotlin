package com.firebase.chat.ui.viewmodel

import android.util.Patterns
import androidx.databinding.ObservableField
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.interacter.ForgotPasswordUseCase

class ForgotPasswordViewModel(private val forgotPasswordUseCase: ForgotPasswordUseCase): BaseViewModel(forgotPasswordUseCase) {

    fun checkValidation(): Boolean {
        val email = forgotEmail.get()
        if (email.isNullOrEmpty()) {
            forgotPasswordUseCase.getContext().toast(forgotPasswordUseCase.getContext().getString(R.string.alert_enter_email))
            return false
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            forgotPasswordUseCase.getContext().toast(forgotPasswordUseCase.getContext().getString(R.string.alert_enter_valid_email))
            return false
        }else {
            return true
        }
    }

    val forgotEmail = ObservableField("")
}