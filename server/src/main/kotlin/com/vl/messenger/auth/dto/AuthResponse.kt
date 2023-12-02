package com.vl.messenger.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Min

class AuthResponse(
    @field:JsonProperty("access_token", required = true)
    val token: String,
    @field:JsonProperty("expires_in")
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:Min(0)
    val expiration: Int? = null
)