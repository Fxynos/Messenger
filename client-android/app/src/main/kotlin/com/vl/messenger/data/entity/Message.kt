package com.vl.messenger.data.entity

class Message(
    val id: Long,
    val senderId: Int,
    val timestamp: Long,
    val content: String
)