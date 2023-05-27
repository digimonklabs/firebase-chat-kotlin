package com.mobisharnam.domain.model

data class InvitationResponse (
    val invitation: ArrayList<Invitation>,
    val exception: Exception?
)
