package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

class PrivateMessageForm(
    @field:NotNull
    @field:JsonProperty("user_id")
    private val userId: Int?,
    val content: String
) {
    val receiverId: Int
        get() = userId!!
}