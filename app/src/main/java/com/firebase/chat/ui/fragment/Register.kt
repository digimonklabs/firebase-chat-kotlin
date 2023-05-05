package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentRegisterBinding
import com.firebase.chat.ui.viewmodel.RegisterViewModel
import com.mobisharnam.domain.util.AppConstant
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.model.firebasedb.User
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

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

    }

    fun onLoginClick() {
        viewModel.navigate(RegisterDirections.registerToLoginFragment())
    }

    fun onRegisterClick() {
        val rootReferences = viewModel.getDataBaseReference()
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
                    val userTable = User(
                        user?.uid ?: "",
                        viewModel.registerName.get().toString(),
                        viewModel.registerEmail.get().toString(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        true,
                        "",
                        HashMap(),
                        ""
                    )

                    rootReferences.child(AppConstant.USER_TABLE).child(user!!.uid).setValue(userTable)
                        .addOnSuccessListener {
                            mContext.toast(getString(R.string.register_successfully))
                            viewModel.navigate(RegisterDirections.registerToChattingFragment())
                        }.addOnFailureListener {
                            mContext.toast(it.message.toString())
                        }
                }
            }
        }
    }
}