package com.firebase.chat.ui.fragment

import android.util.Log
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentForgotPasswordBinding
import com.firebase.chat.ui.viewmodel.ForgotPasswordViewModel
import com.firebase.chat.utils.Extension.toast
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPassword : BaseFragment<FragmentForgotPasswordBinding, ForgotPasswordViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_forgot_password
    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            forgotPassword = this@ForgotPassword
            viewModel = this@ForgotPassword.viewModel
        }
    }

    fun onForgetClick() {
        val isValid = viewModel.checkValidation()
        if (isValid) {
            viewModel.getFireBaseInstance().sendPasswordResetEmail(viewModel.forgotEmail.get().toString())
                .addOnCompleteListener(requireActivity()) { task ->
                    when(task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            mContext.toast(getString(R.string.user_does_not_exist))
                        }
                        else -> {
                            mContext.toast(task.exception?.message.toString())
                        }
                    }
                }
                .addOnSuccessListener(requireActivity()) { authResult ->
                    Log.e("PrintresetPassword","addOnSuccessListener -> ${authResult}")
                }
                .addOnFailureListener(requireActivity()) { exception ->
                    mContext.toast(exception.message.toString())
                }
        }
    }
}