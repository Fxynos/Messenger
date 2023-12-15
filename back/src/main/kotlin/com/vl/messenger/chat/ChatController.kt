package com.vl.messenger.chat

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatController(@Autowired private val template: SimpMessagingTemplate) {

    @MessageMapping("/chat")
    fun sendMessage(@Valid @Payload message: ChatMessage) {
        println("got $message")
        template.convertAndSend(ChatMessage(content = "Yo"))
    }
}