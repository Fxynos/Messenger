package com.vl.messenger.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class StatusResponse<T>(
    @field:JsonProperty(required = true)
    @field:Min(100)
    @field:Max(511)
    val code: Int,

    @field:JsonProperty(required = true)
    @field:NotBlank
    val message: String,

    @field:JsonProperty(required = true)
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val response: T?
)