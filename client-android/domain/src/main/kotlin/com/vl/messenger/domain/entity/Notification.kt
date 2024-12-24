package com.vl.messenger.domain.entity

sealed interface Notification {

    val id: Long
    val unixSec: Long
    val isSeen: Boolean

    data class Info(
        override val id: Long,
        override val unixSec: Long,
        override val isSeen: Boolean,
        val title: String,
        val content: String
    ): Notification

    data class FriendRequest(
        override val id: Long,
        override val unixSec: Long,
        override val isSeen: Boolean,
        val sender: User
    ): Notification

    data class InviteToConversation(
        override val id: Long,
        override val unixSec: Long,
        override val isSeen: Boolean,
        val sender: User,
        val dialog: Dialog
    ): Notification
}