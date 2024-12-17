package com.vl.messenger

import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.dto.UsersResponse
import com.vl.messenger.profile.dto.ProfileResponse
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

fun Long.asConversationDialogId() = "c$this"
fun Int.asPrivateDialogId() = "u$this"

/**
 * Used to mark localization codes of message source
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class StringRes