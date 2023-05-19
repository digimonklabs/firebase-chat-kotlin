package com.firebase.chat.base

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.firebase.chat.callback.OnSetAdapter
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
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import java.text.SimpleDateFormat
import java.util.Locale

open class BaseViewModel(private val baseUseCase: BaseUseCase) : ViewModel() {

    val sendInvitationList = ObservableField(ArrayList<Invitation>())
    val noFriend = ObservableBoolean(false)
    val existFriendList = ObservableField(ArrayList<String>())
    val invitationList = ObservableField(ArrayList<String>())
    val userChatList = ObservableField(ArrayList<NewUser>())
    val dateFormat = SimpleDateFormat("hh:mm a,dd-MM-yyyy", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val _navigation = MutableLiveData<Event<NavigationCommand>>()
    val navigation: LiveData<Event<NavigationCommand>> get() = _navigation

    fun getAllUser(onSetAdapter: OnSetAdapter? = null) {
        val userReference = getDataBaseReference().child("USerTable")
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("PrintOnDataChangeAll","getAllUser method call")
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        if (sp.key == getFireBaseAuth().uid) {
                            sp.getValue(NewUser::class.java)?.let {
                                existFriendList.set(it.friendsList)
                                invitationList.set(it.invitationList)
                            }
                        }else {
                            sp.getValue(NewUser::class.java)?.let {
                                userChatList.get()?.add(it)
                            }
                        }
                    }
                    existFriendList.get()?.isEmpty()?.let { noFriend.set(it) }
                    onSetAdapter?.onSetAdapter(AppConstant.ADD_FRIENDS_ADAPTER)
                    Log.e("getAllUser","existFriendList ->  ${existFriendList.get()?.size} invitationList ->  ${invitationList.get()?.size} userChatList ->  ${userChatList.get()?.size}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

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
                snapshot.getValue(NewUser::class.java)?.let {
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