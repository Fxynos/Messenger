package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ConversationService(@Autowired private val dataMapper: DataMapper) { // TODO

    fun createConversation(userId: Int, name: String) = dataMapper.createConversation(userId, name)

    fun getConversation(id: Long) = Unit

    fun getMembers(conversationId: Long) = Unit
}