package com.vl.messenger.chat

import com.vl.messenger.DataMapper
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

    fun userExists(userId: Int) = dataMapper.getVerboseUser(userId) != null

    fun getDialogs(userId: Int) = dataMapper.getDialogs(userId)

    fun sendMessage(userId: Int, receiverId: Int, content: String) {
        val messageId = dataMapper.addMessage(userId, receiverId, content)
        if (registry.getUser(dataMapper.getVerboseUser(receiverId)!!.login) != null)
            template.convertAndSend("/user/$receiverId/chat", StompMessage().apply {
                id = messageId
                senderId = userId
                this.content = content
            })
    }

    fun getPrivateMessages(userId: Int, companionId: Int, fromId: Long?, limit: Int) =
        dataMapper.getPrivateMessages(userId, companionId, fromId, limit)
}