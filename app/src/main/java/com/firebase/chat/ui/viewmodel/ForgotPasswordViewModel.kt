package com.firebase.chat.ui.viewmodel

import android.util.Patterns
import androidx.databinding.ObservableField
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.interacter.ForgotPasswordUseCase

class ForgotPasswordViewModel(private val forgotPasswordUseCase: ForgotPasswordUseCase) :
    BaseViewModel(forgotPasswordUseCase) {

    val forgotEmail = ObservableField("")

    fun checkValidation(): Boolean {
        val email = forgotEmail.get()
        return if (email.isNullOrEmpty()) {
            forgotPasswordUseCase.getContext()
                .toast(forgotPasswordUseCase.getContext().getString(R.string.alert_enter_email))
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            forgotPasswordUseCase.getContext().toast(
                forgotPasswordUseCase.getContext().getString(R.string.alert_enter_valid_email)
            )
            false
        } else {
            true
        }
    }
}