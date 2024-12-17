package com.vl.messenger.data.network

import com.vl.messenger.data.network.dto.ConversationMemberDto
import com.vl.messenger.data.network.dto.ConversationMembersResponse
import com.vl.messenger.data.network.dto.DialogDto
import com.vl.messenger.data.network.dto.DialogResponse
import com.vl.messenger.data.network.dto.FriendStatusDto
import com.vl.messenger.data.network.dto.MessageDto
import com.vl.messenger.data.network.dto.MessagesDto
import com.vl.messenger.data.network.dto.ProfileDto
import com.vl.messenger.data.network.dto.RoleDto
import com.vl.messenger.data.network.dto.RolesResponse
import com.vl.messenger.data.network.dto.StompMessage
import com.vl.messenger.data.network.dto.TokenDto
import com.vl.messenger.data.network.dto.UserDto
import com.vl.messenger.data.network.dto.UsersDto
import com.vl.messenger.domain.entity.AccessToken
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.ExtendedDialog
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.Profile
import com.vl.messenger.domain.entity.Role
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.entity.VerboseUser

internal object NetworkMapper {
    fun TokenDto.toDomain() = AccessToken(token, expirationSec, userId)
    fun ProfileDto.toDomain() = Profile(id, login, image, isHidden)
    fun UserDto.toDomain() = User(id, login, image)
    fun UsersDto.toDomain() = users.map { it.toDomain() }
    fun FriendStatusDto.toDomain() = when (this) {
        FriendStatusDto.NONE -> FriendStatus.NONE
        FriendStatusDto.REQUEST_SENT -> FriendStatus.REQUEST_SENT
        FriendStatusDto.REQUEST_GOTTEN -> FriendStatus.REQUEST_GOTTEN
        FriendStatusDto.FRIEND -> FriendStatus.FRIEND
    }
    fun UserDto.toDomainWithFriendStatus() = VerboseUser(
        user = toDomain(),
        friendStatus = friendStatus!!.toDomain()
    )
    fun UsersDto.toDomainWithFriendStatus() = users.map { it.toDomainWithFriendStatus() }
    fun DialogDto.toDomain() = Dialog(
        id = id,
        isPrivate = when {
            id.startsWith('u') -> true
            id.startsWith('c') -> false
            else -> throw IllegalArgumentException("id=$id")
        },
        title = title,
        image = image
    )
    fun DialogResponse.toDomain() = ExtendedDialog(
        dialog = dialog.toDomain(),
        lastMessage = lastMessage?.run {
            Message(id, sender.id, "", timestamp, content)
        },
        sender = lastMessage?.sender?.run {
            User(id, login, image)
        }
    )
    fun MessageDto.toDomain(dialogId: String) = Message(id, senderId, dialogId, timestamp, content)
    fun MessagesDto.toDomain(dialogId: String) = messages.map { it.toDomain(dialogId) }
    fun StompMessage.toDomain() = Message(id!!, senderId!!, dialogId!!, System.currentTimeMillis(), content)
    fun RoleDto.toDomain() = Role(id, name, canGetReports, canEditData, canEditMembers, canEditRights)
    fun RolesResponse.toDomain() = roles.map { it.toDomain() }
    fun ConversationMemberDto.toDomain() = ConversationMember(User(userId, login, image), role.toDomain())
    fun ConversationMembersResponse.toDomain() = members.map { it.toDomain() }
}