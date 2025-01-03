package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.chat.dto.StompMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Service

@Service
class ChatService(
    @Autowired private val dataMapper: DataMapper,
    @Autowired private val template: SimpMessagingTemplate,
    @Autowired private val registry: SimpUserRegistry
) {
    val roles by dataMapper::roles

    fun userExists(userId: Int) = dataMapper.getVerboseUser(userId) != null

    fun getDialogs(userId: Int, offset: Int, limit: Int) = dataMapper.getDialogs(userId, offset, limit)

    fun getPrivateDialog(userId: Int) = dataMapper.getVerboseUser(userId)?.run {
        DataMapper.Dialog(
            isPrivate = true,
            id = userId.toLong(),
            title = login,
            image = image,
            lastMessage = null,
            lastMessageSender = null
        )
    }

    fun getConversationDialog(conversationId: Long) = dataMapper.getConversation(conversationId)?.run {
        DataMapper.Dialog(
            isPrivate = false,
            id = conversationId,
            title = name,
            image = image,
            lastMessage = null,
            lastMessageSender = null
        )
    }

    fun sendPrivateMessage(userId: Int, receiverId: Int, content: String): MessagesResponse.Message {
        val messageId = dataMapper.addMessage(userId, receiverId, content)
        if (registry.getUser(dataMapper.getVerboseUser(receiverId)!!.login) != null)
            template.convertAndSend(getUserDestination(receiverId), StompMessage().apply {
                id = messageId
                senderId = userId
                dialogId = "u$senderId"
                this.content = content
            })
        return MessagesResponse.Message(messageId, userId, System.currentTimeMillis() / 1000, content)
    }

    fun getPrivateMessages(userId: Int, companionId: Int, fromId: Long?, limit: Int) =
        dataMapper.getPrivateMessages(userId, companionId, fromId, limit)

    fun getConversationMessages(conversationId: Long, fromId: Long?, limit: Int) =
        dataMapper.getConversationMessages(conversationId, fromId, limit)

    fun sendConversationMessage(userId: Int, conversationId: Long, content: String): MessagesResponse.Message {
        val messageId = dataMapper.addConversationMessage(userId, conversationId, content)
        val username = dataMapper.getVerboseUser(userId)!!.login
        val excludeList = arrayOf(null, username) // these users won't be notified

        dataMapper.getAllMembers(conversationId)
            .filter { registry.getUser(it.login)?.name !in excludeList }
            .forEach { receiver ->
                template.convertAndSend(getUserDestination(receiver.id), StompMessage().apply {
                    id = messageId
                    senderId = userId
                    this.content = content
                    this.dialogId = "c$conversationId"
                })
            }
        return MessagesResponse.Message(messageId, userId, System.currentTimeMillis(), content)
    }

    private fun getUserDestination(userId: Int) = "/users/$userId/chat"
}