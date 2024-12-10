package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.dto.DialogResponse
import com.vl.messenger.chat.dto.MessagesResponse
import com.vl.messenger.chat.dto.RoleDto
import com.vl.messenger.chat.dto.RolesResponse
import com.vl.messenger.toDto

object DtoMapper {
    fun DataMapper.Dialog.toDto(baseUrl: String) = DialogResponse(
        isPrivate = isPrivate,
        dialog = DialogResponse.DialogDto(
            id = "${if (isPrivate) "u" else "c"}$id",
            title = title,
            image = image
        ),
        lastMessage = lastMessage?.run {
            DialogResponse.MessageDto(
                id = id,
                timestamp = unixSec,
                content = content,
                sender = lastMessageSender!!.toDto(baseUrl)
            )
        }
    )
    fun DataMapper.Message.toDto() = MessagesResponse.Message(
        id = id,
        timestamp = unixSec,
        content = content,
        senderId = senderId
    )
    fun List<DataMapper.Message>.toDto() = MessagesResponse(
        messages = map { it.toDto() }
    )
    fun DataMapper.ConversationMember.Role.toDto() = RoleDto(
        id = id,
        name = name,
        canGetReports = canGetReports,
        canEditData = canEditData,
        canEditMembers = canEditMembers,
        canEditRights = canEditRights,
    )
    fun List<DataMapper.ConversationMember.Role>.toDto() = RolesResponse(map { it.toDto() })
}