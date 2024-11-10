package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.DtoMapper.toDto
import com.vl.messenger.chat.dto.DialogResponse
import com.vl.messenger.chat.dto.MessageForm
import com.vl.messenger.chat.dto.MessagesResponse
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
    @Autowired private val chatService: ChatService
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
    ): ResponseEntity<StatusResponse<DialogResponse>> = handleDialog(
        id = id,
        privateDialogBlock = { dialog, _ -> dialog.toDto(baseUrl) },
        conversationBlock = { dialog, _ -> dialog.toDto(baseUrl) }
    )

    @GetMapping("/dialogs/{id}/messages")
    fun getMessages(
        @PathVariable id: String,
        @RequestParam("from_id", required = false) fromId: Long?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<MessagesResponse>> = handleDialog(
        id = id,
        privateDialogBlock = { _, companionId ->
            chatService.getPrivateMessages(
                userId = userId,
                companionId = companionId,
                fromId = fromId,
                limit = limit
            ).toDto()
        },
        conversationBlock = { _, conversationId ->
            chatService.getConversationMessages(
                conversationId = conversationId,
                fromId = fromId,
                limit = limit
            ).toDto()
        }
    )

    @PostMapping("/dialogs/{id}/messages")
    fun sendMessage(
        @PathVariable id: String,
        @Valid @RequestBody message: MessageForm
    ): ResponseEntity<StatusResponse<MessagesResponse.Message>> {
        if (message.content.length > 1000)
            return statusOf(HttpStatus.PAYLOAD_TOO_LARGE, "Message is too long")

        return handleDialog(
            id = id,
            privateDialogBlock = { _, companionId ->
                chatService.sendPrivateMessage(
                    userId,
                    companionId,
                    message.content.trim()
                )
            },
            conversationBlock = { _, conversationId ->
                chatService.sendConversationMessage(
                    userId,
                    conversationId,
                    message.content.trim()
                )
            }
        )
    }

    private inline fun <T> handleDialog(
        id: String,
        privateDialogBlock: (dialog: DataMapper.Dialog, userId: Int) -> T,
        conversationBlock: (dialog: DataMapper.Dialog, conversationId: Long) -> T
    ): ResponseEntity<StatusResponse<T>> {
        return when {
            id.startsWith('u') -> { // private dialog
                val userId = id.substring(1).toIntOrNull()
                    ?: return statusOf(HttpStatus.BAD_REQUEST, "Invalid user id")

                val dialog = chatService.getPrivateDialog(userId)
                    ?: return statusOf(HttpStatus.NOT_FOUND, "No such user")

                statusOf(payload = privateDialogBlock(dialog, userId))
            }

            id.startsWith('c') -> { // conversation dialog
                val conversationId = id.substring(1).toLongOrNull()
                    ?: return statusOf(HttpStatus.BAD_REQUEST, "Invalid conversation id")

                val dialog = chatService.getConversationDialog(conversationId)
                    ?: return statusOf(HttpStatus.NOT_FOUND, "No such conversation")

                statusOf(payload = conversationBlock(dialog, conversationId))
            }

            else -> statusOf(HttpStatus.BAD_REQUEST, "Invalid id prefix")
        }
    }
}