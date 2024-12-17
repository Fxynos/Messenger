package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

class StompMessage {
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Long? = null
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("dialog_id")
    var dialogId: String? = null
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("sender_id")
    var senderId: Int? = null
    @field:JsonProperty(required = true)
    lateinit var content: String
}