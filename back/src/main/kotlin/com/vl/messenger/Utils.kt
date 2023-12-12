package com.vl.messenger

import com.vl.messenger.dto.StatusResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder

const val LOGIN_PATTERN = "^\\w{1,20}$"
const val NAME_PATTERN = "^[\\w -]{1,20}\$"

fun <T> statusOf(
    status: HttpStatus = HttpStatus.OK,
    reason: String = status.reasonPhrase,
    payload: T? = null
) = ResponseEntity(StatusResponse(status.value(), reason, payload), status)

val userId: Int
    get() = SecurityContextHolder.getContext().authentication.principal as Int