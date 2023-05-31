package com.firebase.chat.ui.fragment

import android.os.Bundle
import android.view.View
import com.firebase.chat.R
import com.firebase.chat.base.BaseFragment
import com.firebase.chat.databinding.FragmentInvitationBinding
import com.firebase.chat.ui.adapter.InvitationAdapter
import com.firebase.chat.ui.viewmodel.InvitationViewModel
import com.firebase.chat.utils.Extension.toast
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.response.Response
import org.koin.androidx.viewmodel.ext.android.viewModel

class Invitation : BaseFragment<FragmentInvitationBinding, InvitationViewModel>() {

    private val userList = ArrayList<Invitation>()
    override val layoutId: Int
        get() = R.layout.fragment_invitation
    override val viewModel: InvitationViewModel by viewModel()

    override fun setVariable() {
        binding.apply {
            invitationViewModel = viewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getInvitation()
    }

    override fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onPersistentViewCreated(view, savedInstanceState)

        setUpInvitationObserver()
    }

    private fun setUpInvitationObserver() {
        viewModel.invitationLiveData.observe(viewLifecycleOwner) { response ->
            response.getContentIfNotHandled()?.let { invitation ->
                when (invitation.status) {
                    Response.Status.SUCCESS -> {
                        userList.clear()
                        invitation.data?.let { invitationList ->
                            viewModel.noInvitation.set(invitationList.isEmpty())
                            userList.addAll(invitationList)
                        }
                        binding.adapter = InvitationAdapter(userList, viewModel)
                    }

                    Response.Status.ERROR -> {
                        mContext.toast(invitation.message.toString())
                    }

                    Response.Status.EXCEPTION -> {
                        mContext.toast(invitation.message.toString())
                    }
                }
            }
        }
    }
}