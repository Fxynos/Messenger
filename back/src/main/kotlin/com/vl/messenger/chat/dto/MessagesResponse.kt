package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

class MessagesResponse(
    val messages: List<@Valid Message>
) {
    class Message(
        val id: Long,
        @field:JsonProperty("sender_id", required = true)
        val senderId: Int,
        val timestamp: Long, // unix seconds
        val content: String
    )
}