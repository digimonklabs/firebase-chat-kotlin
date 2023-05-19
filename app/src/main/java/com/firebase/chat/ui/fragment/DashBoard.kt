package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.databinding.FragmentDashBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashBoard : BaseFragment<FragmentDashBoardBinding, BaseViewModel>() {

    private lateinit var auth: FirebaseAuth
    override val layoutId: Int
        get() = R.layout.fragment_dash_board
    override val viewModel: BaseViewModel by viewModel()

    override fun setVariable() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.database.setPersistenceEnabled(true)
        Firebase.database.setPersistenceCacheSizeBytes(100 * 1000 * 1000)
        FirebaseDatabase.getInstance().getReference(AppConstant.USER_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference("USerTable").keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.CHAT_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.TYPING_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.NOTIFICATION_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.READ_STATUS).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.PENDING_MESSAGE_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.FRIEND_TABLE).keepSynced(true)


       /* viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(
            "8fCP4TXjEbXC7nD3K62XwiWXMXq1").child("friendsList").child(viewModel.getFireBaseAuth().uid.toString()).setValue(friends)*/

        /*val user = User(
            viewModel.getFireBaseAuth().uid.toString(),
            userName = "Jeel Bhut",
            userEmail = "jeelbhut007@gmail.com",
            token = "dLashqiTTPO-y-KDj4XeZo:APA91bFEWHyM9_glSqaRiNehoy9uL7yMweLOqbDq8OwcB0f99FpLn8NgARiIYGKQOzv57iXTqs2E3TjtfpyhkiaejVc6CUALIv2GaJ72w0rj0oUAS9Vs2LuUHQSD-gZSSX75UTFA-_LE",
            online = false,
            lastMessage = "dhfdh",
            created = 1683290006992,
            updated = 1683290006992,
            friendsList = hashMapOf(
                "8fCP4TXjEbXC7nD3K62XwiWXMXq1" to FriendsList(lastMessage = "hii", dateTime = 1683008130150L),
                "8fCP4TXjEbXC7nD3K62XwiWXMXq2" to FriendsList(lastMessage = "Hello", dateTime = 1683008130150L)
            )
        )

        val  friendsList = hashMapOf(
            "CmBcBN4R2OWeFG2D9seU5Erbdda3" to FriendsList(lastMessage = "hii", dateTime = 1683008130150L),
            "CmBcBN4R2OWeFG2D9seU5Erbdda4" to FriendsList(lastMessage = "", dateTime = 1683008130150L)
        )


        viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(viewModel.getFireBaseAuth().uid.toString()).setValue(user)*/


        auth = viewModel.getFireBaseAuth()
        if (auth.currentUser == null) {
            viewModel.navigate(DashBoardDirections.dashBoardToLoginFragment())
        } else {
            viewModel.navigate(DashBoardDirections.dashBoardToChattingFragment())
        }
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        viewModel.getFireBaseAuth().uid?.let { uid ->
            viewModel.currentUserName(uid)
            viewModel.setToken(uid)
            viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(
                AppConstant.USER_ONLINE).ref.setValue(true)
            viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(
                AppConstant.USER_ONLINE).ref.onDisconnect().setValue(false)
        }
    }
}