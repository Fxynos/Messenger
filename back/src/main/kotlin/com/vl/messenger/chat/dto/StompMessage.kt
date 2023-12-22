package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

class StompMessage {
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Long? = null
    @field:JsonProperty(required = true)
    lateinit var content: String
}