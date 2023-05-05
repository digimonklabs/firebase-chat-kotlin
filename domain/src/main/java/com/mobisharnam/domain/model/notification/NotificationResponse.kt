package com.mobisharnam.domain.model.notification

data class NotificationResponse(
    val canonical_ids: Int,
    val failure: Int,
    val multicast_id: Long,
    val results: List<Result>,
    val success: Int
)