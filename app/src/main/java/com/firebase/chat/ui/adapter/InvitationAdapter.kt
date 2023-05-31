package com.firebase.chat.ui.adapter

import com.daily.quotes.base.BaseAdapters
import com.daily.quotes.base.BaseViewHolder
import com.firebase.chat.R
import com.firebase.chat.databinding.ItemInvitationLayoutBinding
import com.firebase.chat.ui.viewmodel.InvitationViewModel
import com.mobisharnam.domain.model.Invitation

class InvitationAdapter(
    userList: ArrayList<Invitation>,
    viewModel: InvitationViewModel,
): BaseAdapters<ItemInvitationLayoutBinding, InvitationViewModel, Invitation>(userList, viewModel) {

    override val layoutId: Int
        get() = R.layout.item_invitation_layout

    override fun bind(
        binding: ItemInvitationLayoutBinding,
        item: Invitation,
        position: Int,
        holder: BaseViewHolder<ItemInvitationLayoutBinding>,
        viewModel: InvitationViewModel
    ) {
        binding.apply {
            user = item
            try {
                val name = item.senderName.split(" ")
                ivUserImage.text = name[0][0] + name[1][0].toString()
            } catch (e: Exception) {
                ivUserImage.text = item.senderName.first().toString()
            }

            btnAccept.setOnClickListener {
                //viewModel.acceptInvitation(item.uid,true)
                viewModel.acceptInvitation(item.senderId,true)
            }

            btnDenied.setOnClickListener {
                //viewModel.acceptInvitation(item.uid,false)
                viewModel.acceptInvitation(item.senderId,false)
            }
        }
    }
}