package com.firebase.chat.ui.fragment

import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentRegisterBinding
import com.firebase.chat.ui.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class Register : BaseFragment<FragmentRegisterBinding, RegisterViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_register
    override val viewModel: RegisterViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            register = this@Register
            viewModel = this@Register.viewModel
        }
    }

    fun onLoginClick() {
        viewModel.navigate(RegisterDirections.registerToLoginFragment())
    }

    fun onRegisterClick() {
        val isValid = viewModel.checkValidation()
        if (isValid) {
            val firebaseAuth = viewModel.getFireBaseAuth()
            firebaseAuth.createUserWithEmailAndPassword(
                viewModel.registerEmail.get().toString(),
                viewModel.registerPassword.get().toString()
            ).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.uid?.let {
                        viewModel.setToken(it)
                    }
                    viewModel.registerNewUser(user)
                }
            }
        }
    }
}