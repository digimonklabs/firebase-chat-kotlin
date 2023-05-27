package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.databinding.FragmentDashBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
        FirebaseDatabase.getInstance().getReference(AppConstant.CHAT_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.FRIEND_TABLE).keepSynced(true)
        FirebaseDatabase.getInstance().getReference(AppConstant.INVITATION_TABLE).keepSynced(true)

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
            Log.e("PrintUid","PrintUid -> $uid")
            viewModel.currentUserName(uid)
            viewModel.setToken(uid)
            viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(
                AppConstant.USER_ONLINE).ref.setValue(true)
            viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(
                AppConstant.USER_ONLINE).ref.onDisconnect().setValue(false)
            viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(uid).child(
                AppConstant.LAST_SEEN).ref.onDisconnect().setValue(System.currentTimeMillis())
        }
    }
}