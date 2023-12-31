package com.vl.messenger.chat

import com.vl.messenger.chat.dto.MessageForm
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.dto.UsersResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatRestController(
    @Autowired private val chatService: ChatService
) {
    @GetMapping("/dialogs")
    fun getDialogs(): ResponseEntity<StatusResponse<UsersResponse>> {
        return statusOf(payload = UsersResponse(chatService.getDialogs(userId).map {
            UsersResponse.User(it.id, it.login, it.image)
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
            chatService.getPrivateMessages(userId, companionId, fromId, limit).map {
                MessagesResponse.Message(it.id, it.senderId, it.unixSec, it.content)
            }
        ))
    }

    @PostMapping("/messages/private/send")
    fun sendPrivateMessageOverRest(
        @Valid @RequestBody message: MessageForm
    ): ResponseEntity<StatusResponse<Nothing?>> {
        if (!chatService.userExists(message.receiverId))
            return statusOf(HttpStatus.GONE, "No such user")
        if (message.content.length > 1000)
            return statusOf(HttpStatus.PAYLOAD_TOO_LARGE, "Message is too long")
        chatService.sendMessage(userId, message.receiverId, message.content.trim())
        return statusOf(HttpStatus.OK, "Message is sent")
    }
}