package com.vl.messenger.data.network.dto

internal class NotificationDto {

    val type: Type = Type.INFO
    val id: Long = 0
    val time: Long = 0
    val seen: Boolean = false
    val sender: UserDto? = null
    val dialog: DialogDto? = null
    val title: String? = null
    val content: String? = null

    enum class Type {
        INFO,
        FRIEND_REQUEST,
        CONVERSATION_INVITE
    }
}