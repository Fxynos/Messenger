package com.vl.messenger.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

class UsersResponse(
    @field:JsonProperty(required = true)
    val users: List<@Valid UserDto>
) {
    class UserDto(
        @field:JsonProperty(required = true)
        val id: Int,
        @field:JsonProperty(required = true)
        val login: String,
        @field:JsonProperty("image_url")
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        val image: String?,
        @field:JsonProperty("friend_status")
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        val friendStatus: FriendStatus? = null
    )

    enum class FriendStatus {
        NONE,
        REQUEST_SENT,
        REQUEST_GOTTEN,
        FRIEND
    }
}