package com.vl.messenger.profile.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.vl.messenger.chat.dto.DialogResponse
import com.vl.messenger.dto.UsersResponse
import jakarta.validation.Valid

class NotificationsResponse(
    val notifications: List<@Valid Notification>
) {
    class Notification(
        val type: Type,
        val id: Long,
        val time: Long,
        val seen: Boolean,
        @field:JsonInclude(Include.NON_NULL)
        @field:JsonProperty("sender")
        val sender: UsersResponse.UserDto?,
        @field:JsonInclude(Include.NON_NULL)
        @field:JsonProperty("dialog")
        val conversation: DialogResponse.DialogDto?,
        @field:JsonInclude(Include.NON_NULL)
        val title: String?,
        @field:JsonInclude(Include.NON_NULL)
        val content: String?
    ) {
        constructor(id: Long, time: Long, seen: Boolean, title: String, content: String):
                this(Type.INFO, id, time, seen, null, null, title, content)

        constructor(id: Long, time: Long, seen: Boolean, sender: UsersResponse.UserDto):
                this(Type.FRIEND_REQUEST, id, time, seen, sender, null, null, null)

        constructor(id: Long, time: Long, seen: Boolean, sender: UsersResponse.UserDto, dialog: DialogResponse.DialogDto):
                this(Type.CONVERSATION_INVITE, id, time, seen, sender, dialog, null, null)

        enum class Type {
            INFO,
            FRIEND_REQUEST,
            CONVERSATION_INVITE
        }
    }
}
