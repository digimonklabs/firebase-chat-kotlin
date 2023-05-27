package com.firebase.chat.ui.viewmodel

import android.util.Patterns
import androidx.databinding.ObservableField
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.ui.fragment.RegisterDirections
import com.firebase.chat.utils.Extension.toast
import com.google.firebase.auth.FirebaseUser
import com.mobisharnam.domain.interacter.RegisterUseCase
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import java.util.Random

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

    fun registerNewUser(user: FirebaseUser?) {
        val userTable = NewUser(
            uid = user?.uid ?: "",
            userName = registerName.get().toString(),
            userEmail = registerEmail.get().toString(),
            created = System.currentTimeMillis(),
            updated = System.currentTimeMillis(),
            online = true,
            friendsList = ArrayList(),
            invitationList = ArrayList(),
            token = "",
            notificationId = Random().nextInt(5001),
            lastSeen = System.currentTimeMillis()
        )

        getDataBaseReference().child(AppConstant.USER_TABLE).child(user!!.uid)
            .setValue(userTable)
            .addOnSuccessListener {
                registerUseCase.getContext().toast(registerUseCase.getContext().getString(R.string.register_successfully))
                user.uid.let {
                    currentUserName(it)
                    setToken(it)
                    getDataBaseReference().child(AppConstant.USER_TABLE)
                        .child(it).child(
                            AppConstant.USER_ONLINE
                        ).ref.setValue(true)
                    getDataBaseReference().child(AppConstant.USER_TABLE)
                        .child(it).child(
                            AppConstant.USER_ONLINE
                        ).ref.onDisconnect().setValue(false)
                    getDataBaseReference().child(AppConstant.USER_TABLE)
                        .child(it).child(
                            AppConstant.LAST_SEEN
                        ).ref.onDisconnect().setValue(System.currentTimeMillis())
                }
                navigate(RegisterDirections.registerToChattingFragment())
            }.addOnFailureListener {
                registerUseCase.getContext().toast(it.message.toString())
            }
    }

    val registerEmail = ObservableField("")
    val registerName = ObservableField("")
    val registerPassword = ObservableField("")
}