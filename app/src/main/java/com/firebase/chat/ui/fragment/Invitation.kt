package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.callback.OnSetAdapter
import com.firebase.chat.databinding.FragmentInvitationBinding
import com.firebase.chat.ui.adapter.InvitationAdapter
import com.firebase.chat.ui.viewmodel.InvitationViewModel
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class Invitation : BaseFragment<FragmentInvitationBinding, InvitationViewModel>(), OnSetAdapter {

    private val userList = ArrayList<NewUser>()
    override val layoutId: Int
        get() = R.layout.fragment_invitation
    override val viewModel: InvitationViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
           invitationViewModel = viewModel
        }
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        viewModel.getInvitation(this)
    }

    override fun onSetAdapter(adapter: String) {
        if (adapter == AppConstant.INVITATION_ADAPTER) {
            viewModel.invitations.get()?.let {
                userList.clear()
                Log.e("Invitation","onSetAdapter call")
                userList.addAll(it)
                binding.rvInvitations.visibility = if (userList.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                binding.adapter = InvitationAdapter(userList,viewModel)
            }
        }
    }
}