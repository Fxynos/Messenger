package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.dto.ConversationMessageForm
import com.vl.messenger.chat.dto.PrivateMessageForm
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.dto.UsersResponse
import com.vl.messenger.statusOf
import com.vl.messenger.toDto
import com.vl.messenger.userId
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatRestController(
    @Value("\${base.url}") private val baseUrl: String,
    @Autowired private val chatService: ChatService,
    @Autowired private val conversationService: ConversationService
) {
    companion object {
        private fun messageToDto(message: DataMapper.Message) =
            MessagesResponse.Message(message.id, message.senderId, message.unixSec, message.content)
    }

    @GetMapping("/dialogs")
    fun getDialogs(): ResponseEntity<StatusResponse<UsersResponse>> {
        return statusOf(payload = UsersResponse(chatService.getDialogs(userId).map {
            it.toDto(baseUrl)
        }))
    }

    @GetMapping("/messages/private")
    fun getPrivateMessages(
        @RequestParam("user_id") companionId: Int,
        @RequestParam("from_id", required = false) fromId: Long?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<MessagesResponse>> {
        if (fromId != null && fromId < 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"from_id\" must be positive")
        if (limit <= 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"limit\" must be positive")
        if (!chatService.userExists(companionId))
            return statusOf(HttpStatus.GONE, "No such user")
        return statusOf(payload = MessagesResponse(
            chatService.getPrivateMessages(userId, companionId, fromId, limit).map(::messageToDto)
        ))
    }

    @PostMapping("/messages/private/send")
    fun sendPrivateMessage(
        @Valid @RequestBody message: PrivateMessageForm
    ): ResponseEntity<StatusResponse<MessagesResponse.Message>> {
        if (!chatService.userExists(message.receiverId))
            return statusOf(HttpStatus.GONE, "No such user")
        if (message.content.length > 1000)
            return statusOf(HttpStatus.PAYLOAD_TOO_LARGE, "Message is too long")
        return statusOf(
            reason = "Message is sent",
            payload = chatService.sendPrivateMessage(userId, message.receiverId, message.content.trim())
        )
    }

    @GetMapping("/messages/conversations/{id}")
    fun getConversationMessages(
        @PathVariable("id") conversationId: Long,
        @RequestParam("from_id", required = false) fromId: Long?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<MessagesResponse>> {
        if (fromId != null && fromId < 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"from_id\" must be positive")
        if (limit <= 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"limit\" must be positive")
        if (!conversationService.isMember(userId, conversationId))
            return statusOf(HttpStatus.GONE, "Not member")
        return statusOf(payload = MessagesResponse(
            chatService.getConversationMessages(conversationId, fromId, limit).map(::messageToDto)
        ))
    }

    @PostMapping("/messages/conversations/{id}/send")
    fun sendConversationMessage(
        @PathVariable("id") conversationId: Long,
        @Valid @RequestBody message: ConversationMessageForm
    ): ResponseEntity<StatusResponse<Nothing?>> {
        if (!conversationService.isMember(userId, conversationId))
            return statusOf(HttpStatus.GONE, "Not member")
        if (message.content.length > 1000)
            return statusOf(HttpStatus.PAYLOAD_TOO_LARGE, "Message is too long")
        chatService.sendConversationMessage(userId, conversationId, message.content.trim())
        return statusOf(HttpStatus.OK, "Message is sent")
    }
}