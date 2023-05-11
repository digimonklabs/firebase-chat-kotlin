package com.firebase.chat.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.firebase.chat.navigation.NavigationCommand
import com.mobisharnam.domain.util.AppConstant
import com.firebase.chat.utils.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mobisharnam.domain.interacter.BaseUseCase
import com.mobisharnam.domain.model.firebasedb.User
import java.text.SimpleDateFormat
import java.util.Locale

open class BaseViewModel(private val baseUseCase: BaseUseCase) : ViewModel() {

    val dateFormat = SimpleDateFormat("hh:mm a,dd-MM-yyyy", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val _navigation = MutableLiveData<Event<NavigationCommand>>()
    val navigation: LiveData<Event<NavigationCommand>> get() = _navigation

    fun navigate(navDirections: NavDirections) {
        _navigation.value = Event(NavigationCommand.ToDirection(navDirections))
    }

    fun navigateBack() {
        _navigation.value = Event(NavigationCommand.Back)
    }

    fun getFireBaseAuth(): FirebaseAuth {
        return baseUseCase.getFireBaseAuth()
    }

    fun getDataBaseReference() : DatabaseReference {
        return baseUseCase.getDataBaseReference()
    }

    fun getFireBaseInstance() : FirebaseAuth {
        return baseUseCase.getFireBaseInstance()
    }

    fun currentUserName(uid: String) {
        val reference = getDataBaseReference().child(AppConstant.USER_TABLE).child(uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(User::class.java)?.let {
                    AppConstant.currentUser = it.userName
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(AppConstant.TOKEN).setValue(it.result)
        }
    }

}