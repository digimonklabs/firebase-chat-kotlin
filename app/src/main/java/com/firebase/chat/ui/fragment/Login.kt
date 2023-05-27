package com.firebase.chat.ui.fragment

import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentLoginBinding
import com.firebase.chat.ui.viewmodel.LoginViewModel
import com.firebase.chat.utils.Extension.toast
import com.google.firebase.auth.FirebaseAuthException
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class Login : BaseFragment<FragmentLoginBinding, LoginViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            login = this@Login
            viewModel = this@Login.viewModel
        }
    }

    fun onRegisterClick() {
        viewModel.navigate(LoginDirections.loginToRegisterFragment())
    }

    fun onLoginClick() {
        val firebaseAuth = viewModel.getFireBaseAuth()
        firebaseAuth.signInWithEmailAndPassword(
            viewModel.loginEmail.get().toString(),
            viewModel.loginPassword.get().toString()
        ).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                mContext.toast(getString(R.string.login_successfully))
                task.result.user?.uid?.let {
                    viewModel.setToken(it)
                    viewModel.currentUserName(it)
                    viewModel.setToken(it)
                    viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(it).child(
                        AppConstant.USER_ONLINE
                    ).ref.setValue(true)
                    viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(it).child(
                        AppConstant.USER_ONLINE
                    ).ref.onDisconnect().setValue(false)

                }
                viewModel.navigate(LoginDirections.loginToChattingFragment())
            } else {
                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                if (errorCode == AppConstant.ERROR_USER_NOT_FOUND) {
                    mContext.toast(getString(R.string.user_does_not_exist))
                } else {
                    mContext.toast(task.exception?.message.toString())
                }
            }
        }
    }

    fun onLoginGoogleClick() {

    }

    fun onForgotPasswordClick() {
        viewModel.navigate(LoginDirections.loginToForgotPasswordFragment())
    }
}