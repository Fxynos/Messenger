package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

class MessageForm(
    @field:JsonProperty("user_id")
    val receiverId: Int,
    val content: String
)