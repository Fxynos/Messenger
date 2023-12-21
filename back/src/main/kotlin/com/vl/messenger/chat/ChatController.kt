package com.vl.messenger.chat

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    @Autowired private val template: SimpMessagingTemplate,
   //@Autowired private val activeUserSto
) {

    @MessageMapping("/chat")
    fun sendMessage(
        @Valid @Payload message: ChatMessage,
        @Header("simpSessionId") sessionId: String,
        auth: Authentication
    ) {
        println("got ${message.content} on $sessionId")
        template.convertAndSendToUser(auth.name, "/user/${auth.principal}/chat", ChatMessage().apply {
            content = "I can answer"
        })
    }
}