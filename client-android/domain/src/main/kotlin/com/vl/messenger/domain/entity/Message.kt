package com.vl.messenger.domain.entity

data class Message(
    val id: Long,
    val senderId: Int,
    val dialogId: String,
    val unixSec: Long,
    val content: String
)