package com.vl.messenger.user

import com.vl.messenger.LOGIN_PATTERN
import com.vl.messenger.DataMapper
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.user.dto.SearchUserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SocialController(@Autowired private val dataMapper: DataMapper) {
    companion object {
        private val LOGIN_REGEX = Regex(LOGIN_PATTERN)
    }

    @GetMapping("/users/search/{pattern}")
    fun searchUser(@PathVariable pattern: String): ResponseEntity<StatusResponse<SearchUserResponse>> {
        if (!pattern.matches(LOGIN_REGEX))
            return statusOf(HttpStatus.BAD_REQUEST, "Login contains illegal character")
        return statusOf(payload = SearchUserResponse(dataMapper.getUsersByLogin(pattern).map {
            SearchUserResponse.User(it.id, it.login, it.image)
        }))
    }

    /*@PutMapping("/friends/send-request")
    fun sendFriendRequest(@RequestParam("user_id") userId: Int): ResponseEntity<StatusResponse<*>> {
        val loggedUserId = SecurityContextHolder.getContext().authentication as Int
    }*/
}