package com.vl.messenger

import com.vl.messenger.dto.StatusResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> statusOf(
    status: HttpStatus = HttpStatus.OK,
    reason: String = status.reasonPhrase,
    payload: T? = null
) = ResponseEntity(StatusResponse(status.value(), reason, payload), status)