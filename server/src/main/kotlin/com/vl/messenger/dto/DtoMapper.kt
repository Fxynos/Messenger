package com.vl.messenger.dto

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.dto.*
import com.vl.messenger.profile.dto.ProfileResponse
import org.springframework.context.MessageSource
import java.util.*

object DtoMapper {
    fun DataMapper.User.toDto(baseUrl: String) = UsersResponse.UserDto(
        id,
        login,
        image at baseUrl
    )
    fun DataMapper.VerboseUser.toDto(baseUrl: String) = ProfileResponse(
        id,
        login,
        image at baseUrl,
        isHidden
    )
    fun DataMapper.Dialog.toDto(baseUrl: String) = DialogResponse(
        isPrivate = isPrivate,
        dialog = DialogResponse.DialogDto(
            id = "${if (isPrivate) "u" else "c"}$id",
            title = title,
            image = image at baseUrl
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
    fun DataMapper.ConversationMember.Role.toDto(messageSource: MessageSource, locale: Locale) = RoleDto(
        id = id,
        name = messageSource.getMessage("role.$name", null, name, locale)!!,
        canGetReports = canGetReports,
        canEditData = canEditData,
        canEditMembers = canEditMembers,
        canEditRights = canEditRights
    )
    fun List<DataMapper.ConversationMember.Role>.toDto(messageSource: MessageSource, locale: Locale) =
        RolesResponse(map { it.toDto(messageSource, locale) })
    fun DataMapper.ConversationMember.toDto(messageSource: MessageSource, locale: Locale, baseUrl: String) =
        MembersResponse.Member(
            userId = id,
            login = login,
            image = image at baseUrl,
            role = role.toDto(messageSource, locale)
        )
    fun List<DataMapper.ConversationMember>.toDto(messageSource: MessageSource, locale: Locale, baseUrl: String) = MembersResponse(
        members = map { it.toDto(messageSource, locale, baseUrl) }
    )

    private infix fun String?.at(baseUrl: String) = this?.let { "$baseUrl/$it" }
}