package com.vl.messenger.ui.entity

data class DialogUi (
    val id: String,
    val isPrivate: Boolean,
    val name: String,
    val imageUrl: String?,
    val lastMessageText: String?,
    val lastMessageSenderName: String?
)