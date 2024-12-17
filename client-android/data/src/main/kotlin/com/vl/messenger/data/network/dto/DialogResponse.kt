package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class DialogResponse {
    @SerializedName("is_private")
    val isPrivate: Boolean = false
    lateinit var dialog: DialogDto
    @SerializedName("last_message")
    val lastMessage: LastMessageDto? = null

    class LastMessageDto {
        val id: Long = 0
        val timestamp: Long = 0
        val content: String = ""
        lateinit var sender: UserDto
    }
}