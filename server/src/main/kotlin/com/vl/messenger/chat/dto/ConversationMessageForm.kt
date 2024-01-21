package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ConversationMessageForm(
    @JsonProperty("content") // single field wrapping trick: without annotation it would be json consisting of string without brackets
    val content: String
)