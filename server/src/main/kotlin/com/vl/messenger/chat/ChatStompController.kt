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
    @Autowired private val chatService: ChatService,
    @Autowired private val conversationService: ConversationService
) {
    @MessageMapping("/chat/user/{id}")
    fun sendPrivateMessage(
        @DestinationVariable("id") receiverId: Int,
        @Valid @Payload message: StompMessage,
        auth: Authentication
    ) {
        if (!chatService.userExists(receiverId))
            throw NoSuchElementException("No such user")
        if (message.content.length > 1000)
            throw IllegalArgumentException("Message is too long")
        chatService.sendPrivateMessage(auth.principal as Int, receiverId, message.content.trim())
    }

    @MessageMapping("/chat/conversation/{id}")
    fun sendConversationMessage(
        @DestinationVariable("id") conversationId: Long,
        @Valid @Payload message: StompMessage,
        auth: Authentication
    ) {
        val userId = auth.principal as Int
        if (!conversationService.isMember(userId, conversationId))
            throw NoSuchElementException("Not member")
        if (message.content.length > 1000)
            throw IllegalArgumentException("Message is too long")
        chatService.sendConversationMessage(userId, conversationId, message.content.trim())
    }
}