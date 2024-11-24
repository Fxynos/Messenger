package com.vl.messenger.profile.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class ProfileResponse(
    @field:JsonProperty(required = true)
    val id: Int,
    @field:JsonProperty(required = true)
    val login: String,
    @field:JsonProperty("image_url")
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val image: String?,
    @get:JsonProperty("is_hidden")
    val isHidden: Boolean
)