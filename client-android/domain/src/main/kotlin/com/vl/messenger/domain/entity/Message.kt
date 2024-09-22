package com.vl.messenger.domain.entity

/**
 * @param dialogId non-null for conversations and `null` for private dialogs
 */
data class Message(
    val id: Long,
    val senderId: Int,
    val dialogId: Long?,
    val timestamp: Long,
    val content: String
)