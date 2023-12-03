package com.vl.messenger.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.vl.messenger.LOGIN_PATTERN
import jakarta.validation.constraints.Pattern

data class RegistrationForm(
    @field:JsonProperty(required = true)
    @field:Pattern(regexp = LOGIN_PATTERN)
    val login: String,
    @field:JsonProperty(required = true)
    val password: String
)