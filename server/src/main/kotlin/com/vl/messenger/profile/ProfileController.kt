package com.vl.messenger.profile

import com.vl.messenger.DataMapper
import com.vl.messenger.LOGIN_PATTERN
import com.vl.messenger.dto.DtoMapper.toDto
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.dto.UsersResponse
import com.vl.messenger.profile.dto.ProfileResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class ProfileController(
    @Value("\${base.url}") private val baseUrl: String,
    @Autowired private val service: ProfileService
) {
    companion object {
        private val LOGIN_REGEX = Regex(LOGIN_PATTERN)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Int): ResponseEntity<StatusResponse<UsersResponse.UserDto>> =
        service.getUser(id)?.let {
            statusOf(payload = userToDtoWithFriendStatus(it))
        } ?: statusOf(HttpStatus.GONE, "No such user")

    @GetMapping("/me")
    fun whoAmI(): ResponseEntity<StatusResponse<ProfileResponse>> =
        statusOf(payload = service.getUser(userId)!!.toDto(baseUrl))

    @PutMapping("/me/visibility") // TODO use single PATCH endpoint for such settings
    fun setHidden(@RequestParam("is_hidden") isHidden: Boolean): ResponseEntity<StatusResponse<Nothing>> {
        service.setHidden(userId, isHidden)
        return statusOf(HttpStatus.OK)
    }

    @PostMapping("/me/set-image", consumes = ["multipart/form-data"])
    fun setProfileImage(@RequestParam image: MultipartFile): ResponseEntity<StatusResponse<Nothing>> {
        if (image.contentType != "image/png" && image.contentType != "image/jpeg")
            return statusOf(HttpStatus.UNPROCESSABLE_ENTITY, "Only png and jpeg images are supported")
        service.setProfileImage(userId, image)
        return statusOf(HttpStatus.OK, "Profile image is set")
    }

    @GetMapping("/search/{pattern}")
    fun searchUser(
        @PathVariable pattern: String,
        @RequestParam("from_id", required = false) fromId: Int?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<UsersResponse>> {
        if (fromId != null && fromId < 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"from_id\" must be positive")
        if (limit <= 0)
            return statusOf(HttpStatus.BAD_REQUEST, "\"limit\" must be positive")
        if (!pattern.matches(LOGIN_REGEX))
            return statusOf(HttpStatus.BAD_REQUEST, "Login contains illegal character")
        val id = userId
        return statusOf(payload = UsersResponse(
            service.searchUsers(pattern, fromId, limit)
                .filter { it.id != id }
                .map(::userToDtoWithFriendStatus)
        ))
    }

    @PutMapping("/add-friend")
    fun addFriend(@RequestParam("user_id") friendId: Int): ResponseEntity<StatusResponse<Nothing>> {
        if (userId == friendId)
            return statusOf(HttpStatus.BAD_REQUEST, "It's forbidden for users to add themselves to friends")
        if (service.getUser(friendId) == null)
            return statusOf(HttpStatus.GONE, "No such user")
        return statusOf(HttpStatus.OK,
            if (service.addFriend(userId, friendId))
                "User is added to friends"
            else
                "Friend request is sent"
        )
    }

    @GetMapping("/friends")
    fun getFriends(): ResponseEntity<StatusResponse<UsersResponse>> {
        return statusOf(payload = UsersResponse(service.getFriends(userId).map { it.toDto(baseUrl) }))
    }

    @DeleteMapping("/friend")
    fun removeFriend(@RequestParam("user_id") friendId: Int): ResponseEntity<StatusResponse<Nothing>> {
        return statusOf(HttpStatus.OK,
            if (service.removeFriend(userId, friendId))
                "Removed from friends"
            else
                "Friend request is revoked"
        )
    }

    @PostMapping("/block")
    fun addToBlacklist(@RequestParam("user_id") blockedId: Int): ResponseEntity<StatusResponse<Nothing>> { // TODO forbid blocked users to send requests and messages
        return if (service.addToBlacklist(userId, blockedId))
            statusOf(HttpStatus.OK, "User is blocked")
        else
            statusOf(HttpStatus.CONFLICT, "User is already blocked")
    }

    @DeleteMapping("/block")
    fun removeFromBlacklist(@RequestParam("user_id") blockedId: Int): ResponseEntity<StatusResponse<Nothing>> {
        return if (service.removeFromBlacklist(userId, blockedId))
            statusOf(HttpStatus.OK, "User is unblocked")
        else
            statusOf(HttpStatus.CONFLICT, "User wasn't blocked")
    }

    @GetMapping("/blocked")
    fun getBlacklist(): ResponseEntity<StatusResponse<UsersResponse>> {
        return statusOf(payload = UsersResponse(service.getBlacklist(userId).map { it.toDto(baseUrl) }))
    }

    private fun userToDtoWithFriendStatus(user: DataMapper.User): UsersResponse.UserDto {
        val id = userId
        return UsersResponse.UserDto(
            user.id,
            user.login,
            if (user.image == null) null else "$baseUrl/${user.image}",
            when { // TODO reduce requests to data mapper
                service.isFriend(id, user.id) -> UsersResponse.FriendStatus.FRIEND
                service.hasRequestFrom(id, user.id) -> UsersResponse.FriendStatus.REQUEST_GOTTEN
                service.hasRequestFrom(user.id, id) -> UsersResponse.FriendStatus.REQUEST_SENT
                else -> UsersResponse.FriendStatus.NONE
            }
        )
    }
}