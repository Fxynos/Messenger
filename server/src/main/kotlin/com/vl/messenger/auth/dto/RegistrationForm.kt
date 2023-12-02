package com.vl.messenger.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RegistrationForm(
    @field:JsonProperty(required = true) val login: String,
    @field:JsonProperty(required = true) val password: String
)