package com.vl.messenger.chat

import com.vl.messenger.chat.dto.MessageForm
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.dto.StatusResponse
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
    @GetMapping("/messages/private")
    fun getPrivateMessages(
        @RequestParam("user_id") companionId: Int
    ): ResponseEntity<StatusResponse<MessagesResponse>> {
        if (!chatService.userExists(companionId))
            return statusOf(HttpStatus.GONE, "No such user")
        return statusOf(payload = MessagesResponse(chatService.getPrivateMessages(userId, companionId).map {
            MessagesResponse.Message(it.id, it.senderId, it.unixSec, it.content)
        }))
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