package com.vl.messenger.chat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class ChatMessage(
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    @field:JsonProperty(required = true)
    val content: String
)