package com.vl.messenger.ui.entity

/**
 * @param isLastMessageSent is logged user a sender of the message
 */
data class DialogUi (
    val id: String,
    val isPrivate: Boolean,
    val name: String,
    val imageUrl: String?,
    val lastMessageText: String?,
    val lastMessageSenderName: String?
)