package com.vl.messenger.user

import com.vl.messenger.LOGIN_PATTERN
import com.vl.messenger.DataMapper
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.user.dto.SearchUserResponse
import com.vl.messenger.userId
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
@RequestMapping("/users")
class SocialController(@Autowired private val socialService: SocialService) {
    companion object {
        private val LOGIN_REGEX = Regex(LOGIN_PATTERN)
    }

    @GetMapping("/search/{pattern}")
    fun searchUser(@PathVariable pattern: String): ResponseEntity<StatusResponse<SearchUserResponse>> {
        val id = userId
        if (!pattern.matches(LOGIN_REGEX))
            return statusOf(HttpStatus.BAD_REQUEST, "Login contains illegal character")
        return statusOf(payload = SearchUserResponse(socialService.searchUsers(pattern).filter {
            it.id != id
        }.map {
            SearchUserResponse.User(it.id, it.login, it.image)
        }))
    }

    @PutMapping("/add-friend")
    fun sendFriendRequest(@RequestParam("user_id") friendId: Int): ResponseEntity<StatusResponse<Any>> {
        if (userId == friendId)
            return statusOf(HttpStatus.BAD_REQUEST, "It's forbidden for users to add themselves to friends")
        return statusOf(HttpStatus.OK,
            if (socialService.addFriend(userId, friendId))
                "User is added to friends"
            else
                "Friend request is sent"
        )
    }
}