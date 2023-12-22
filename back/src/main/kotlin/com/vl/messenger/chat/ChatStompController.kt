package com.vl.messenger.chat

import com.vl.messenger.chat.dto.StompMessage
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

@Controller
class ChatStompController(
    @Autowired private val chatService: ChatService
) {
    @MessageMapping("/chat/user/{id}")
    fun sendPrivateMessageOverStomp(
        @DestinationVariable("id") receiverId: Int,
        @Valid @Payload message: StompMessage,
        auth: Authentication
    ) {
        if (!chatService.userExists(receiverId))
            throw NoSuchElementException("No such user")
        if (message.content.length > 1000)
            throw IllegalArgumentException("Message is too long")
        chatService.sendMessage(auth.principal as Int, receiverId, message.content.trim())
    }
}