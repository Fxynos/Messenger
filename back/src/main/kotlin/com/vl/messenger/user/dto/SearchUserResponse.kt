package com.vl.messenger.user.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

class SearchUserResponse(
    @field:JsonProperty(required = true)
    val users: List<@Valid User>
) {
    class User(
        @field:JsonProperty(required = true)
        val id: Int,
        @field:JsonProperty(required = true)
        val login: String,
        @field:JsonProperty("image_url")
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        val image: String?
    )
}