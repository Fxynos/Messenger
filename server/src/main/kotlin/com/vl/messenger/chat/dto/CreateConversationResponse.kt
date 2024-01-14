package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

class CreateConversationResponse(
    @field:JsonProperty("conversation_id") val conversationId: Long
)