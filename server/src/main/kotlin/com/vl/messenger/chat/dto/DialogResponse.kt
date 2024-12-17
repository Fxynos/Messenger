package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.vl.messenger.dto.UsersResponse

class DialogResponse(
    @get:JsonProperty("is_private")
    val isPrivate: Boolean,
    val dialog: DialogDto,
    @field:JsonProperty("last_message")
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val lastMessage: MessageDto?
) {
    class DialogDto(
        val id: String,
        val title: String,
        val image: String?
    )

    class MessageDto(
        val id: Long,
        val timestamp: Long,
        val content: String,
        val sender: UsersResponse.UserDto
    )
}