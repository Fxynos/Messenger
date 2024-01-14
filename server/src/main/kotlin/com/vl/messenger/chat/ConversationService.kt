package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ConversationService(@Autowired private val dataMapper: DataMapper) { // TODO

    fun createConversation(userId: Int, name: String) = dataMapper.createConversation(userId, name)

    fun getConversation(userId: Int, conversationId: Long) =
        if (isMember(userId, conversationId))
            dataMapper.getConversation(conversationId)
        else null

    @Throws(IllegalAccessException::class)
    fun setConversationName(userId: Int, conversationId: Long, name: String) =
        if (dataMapper.getRole(userId, conversationId)?.canEditData == true)
            dataMapper.setConversationName(conversationId, name)
        else throw IllegalAccessException("No edit conversation privilege")

    fun getMembers(userId: Int, conversationId: Long) =
        if (isMember(userId, conversationId))
            dataMapper.getMembers(conversationId)
        else null

    /**
     * @param userId id of competent user having privilege to manage members
     * @param memberId id of user being added
     */
    @Throws(IllegalAccessException::class)
    fun addMember(userId: Int, conversationId: Long, memberId: Int) =
        if (dataMapper.getRole(userId, conversationId)?.canEditMembers == true)
            dataMapper.addMember(memberId, conversationId)
        else throw IllegalAccessException("No edit members privilege")

    @Throws(IllegalAccessException::class)
    fun removeMember(userId: Int, conversationId: Long, memberId: Int) =
        if (dataMapper.getRole(userId, conversationId)?.canEditMembers == true)
            dataMapper.removeMember(memberId, conversationId)
        else throw IllegalAccessException("No edit members privilege")

    fun leaveConversation(userId: Int, conversationId: Long) =
        dataMapper.removeMember(userId, conversationId)

    @Throws(IllegalAccessException::class)
    fun setMemberRole(userId: Int, conversationId: Long, memberId: Int, role: String) =
        if (dataMapper.getRole(userId, conversationId)?.canEditRights == true)
            dataMapper.setRole(memberId, conversationId, role)
        else throw IllegalAccessException("No edit rights privilege")

    private fun isMember(userId: Int, conversationId: Long) =
        dataMapper.getRole(userId, conversationId) != null
}