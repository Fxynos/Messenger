package com.vl.messenger.chat

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    @Autowired private val template: SimpMessagingTemplate
) {

    @MessageMapping("/app/chat/{name}")
    fun sendMessage(
        @DestinationVariable name: String,
        @Valid @Payload message: ChatMessage,
        auth: Authentication
    ) {
        println("received on $name (${auth.name}): ${message.content}")
        template.convertAndSend("/user/$name", ChatMessage().apply { content = "i got it: ${message.content}" })
    }
}