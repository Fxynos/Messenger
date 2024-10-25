package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.DtoMapper.toDto
import com.vl.messenger.chat.dto.ConversationMessageForm
import com.vl.messenger.chat.dto.DialogResponse
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.chat.dto.PrivateMessageForm
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    private fun String?.toImageUrl() = if (this == null) null else "$baseUrl/$this"

    /**
     * @return private dialogs and conversations
     */
    @GetMapping("/dialogs")
    fun getDialogs(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<List<DialogResponse>>> {
        return statusOf(
            payload = chatService.getDialogs(userId, offset, limit)
                .map { it.toDto(baseUrl) }
        )
    }

    /**
     * @param id starts with `u` or `c` prefix for private dialogs and conversations respectively,
     * ends with its actual id. For example, to retrieve private dialog for user with id `1234`, `u1234` should be used
     */
    @GetMapping("/dialogs/{id}")
    fun getDialog(
        @PathVariable id: String
    ): ResponseEntity<StatusResponse<DialogResponse>> {
        return when {
            id.startsWith('u') -> { // private dialog
                val userId = id.substring(1).toIntOrNull()
                    ?: return statusOf(HttpStatus.BAD_REQUEST, "Invalid user id")

                val dialog = chatService.getPrivateDialog(userId)
                    ?: return statusOf(HttpStatus.NOT_FOUND, "No such user")

                statusOf(payload = dialog.toDto(baseUrl))
            }

            id.startsWith('c') -> { // conversation dialog
                val conversationId = id.substring(1).toLongOrNull()
                    ?: return statusOf(HttpStatus.BAD_REQUEST, "Invalid conversation id")

                val dialog = chatService.getConversationDialog(conversationId)
                    ?: return statusOf(HttpStatus.NOT_FOUND, "No such conversation")

                statusOf(payload = dialog.toDto(baseUrl))
            }

            else -> statusOf(HttpStatus.BAD_REQUEST, "Invalid id prefix")
        }
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