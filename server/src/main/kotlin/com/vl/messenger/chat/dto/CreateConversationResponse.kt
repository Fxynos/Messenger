package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

class CreateConversationResponse(
    @field:JsonProperty("dialog_id") val dialogId: String
)