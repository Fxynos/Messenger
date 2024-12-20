package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.vl.messenger.LOGIN_PATTERN
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

class MembersResponse(
    val members: List<@Valid Member>
) {
    class Member(
        @field:JsonProperty("user_id")
        val userId: Int,
        @field:Pattern(regexp = LOGIN_PATTERN)
        val login: String,
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        val image: String?,
        val role: RoleDto
    )
}