package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentRegisterBinding
import com.firebase.chat.ui.viewmodel.RegisterViewModel
import com.mobisharnam.domain.util.AppConstant
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.model.firebasedb.User
import java.util.Random
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

                    val userTable = NewUser(
                        uid = user?.uid ?: "",
                        userName = viewModel.registerName.get().toString(),
                        userEmail = viewModel.registerEmail.get().toString(),
                        created = System.currentTimeMillis(),
                        updated = System.currentTimeMillis(),
                        online = true,
                        friendsList = ArrayList(),
                        invitationList = ArrayList(),
                        token = "",
                        notificationId = Random().nextInt(5001)
                    )

                    rootReferences.child("USerTable").child(user!!.uid).setValue(userTable)
                        .addOnSuccessListener {
                            mContext.toast(getString(R.string.register_successfully))
                            user.uid.let {
                                viewModel.currentUserName(it)
                                viewModel.setToken(it)
                                viewModel.getDataBaseReference().child("USerTable").child(it).child(
                                    AppConstant.USER_ONLINE).ref.setValue(true)
                                viewModel.getDataBaseReference().child("USerTable").child(it).child(
                                    AppConstant.USER_ONLINE).ref.onDisconnect().setValue(false)
                            }
                            viewModel.navigate(RegisterDirections.registerToChattingFragment())
                        }.addOnFailureListener {
                            mContext.toast(it.message.toString())
                        }

                    /*rootReferences.child(AppConstant.USER_TABLE).child(user!!.uid).setValue(userTable)
                        .addOnSuccessListener {
                            mContext.toast(getString(R.string.register_successfully))
                            user.uid.let {
                                viewModel.currentUserName(it)
                                viewModel.setToken(it)
                                viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(it).child(
                                    AppConstant.USER_ONLINE).ref.setValue(true)
                                viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(it).child(
                                    AppConstant.USER_ONLINE).ref.onDisconnect().setValue(false)
                            }
                            viewModel.navigate(RegisterDirections.registerToChattingFragment())
                        }.addOnFailureListener {
                            mContext.toast(it.message.toString())
                        }*/
                }
            }
        }
    }
}