package com.vl.messenger.domain.entity

data class ExtendedDialog(
    val dialog: Dialog,
    val lastMessage: Message?,
    val sender: User?
)