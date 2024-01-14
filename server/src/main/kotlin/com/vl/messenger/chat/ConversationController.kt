package com.vl.messenger.chat

import com.vl.messenger.chat.dto.CreateConversationResponse
import com.vl.messenger.chat.dto.DescribeConversationResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Provides interface to create and manage conversations.
 * Use chat controllers to exchange messages.
 * @see ChatRestController
 * @see ChatStompController
 */
@RestController
@RequestMapping("/conversations")
class ConversationController(@Autowired private val service: ConversationService) {
    @PostMapping
    fun createConversation(
        @NotBlank @RequestParam name: String
    ): ResponseEntity<StatusResponse<CreateConversationResponse>> {
        if (name.isBlank())
            return statusOf(HttpStatus.BAD_REQUEST, "Name is blank")
        if (name.length > 20)
            return statusOf(HttpStatus.BAD_REQUEST, "Name is too long")
        return statusOf(payload = CreateConversationResponse(service.createConversation(userId, name)))
    }

    @GetMapping("/{id}")
    fun describeConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<DescribeConversationResponse>> {
        val conversation = service.getConversation(userId, id)
            ?: return statusOf(HttpStatus.GONE, "No conversation or you are not its member")
        return statusOf(payload = DescribeConversationResponse(
            conversation.id,
            conversation.name,
            conversation.image,
            service.getMembers(userId, id)!!.map {
                DescribeConversationResponse.Member(it.id, it.login, it.image, it.role.name)
            }
        ))
    }

    @PutMapping("/{id}/set-name")
    fun setConversationName(
        @PathVariable id: Long,
        @RequestParam name: String
    ): ResponseEntity<StatusResponse<Nothing>> {
        try {
            service.setConversationName(userId, id, name)
            return statusOf(HttpStatus.OK, "Conversation name is set")
        } catch (exception: IllegalAccessException) {
            return statusOf(HttpStatus.FORBIDDEN, exception.message!!)
        }
    }

    @PutMapping("/{id}/leave")
    fun leaveConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<Nothing>> {
        service.leaveConversation(userId, id)
        return statusOf(HttpStatus.OK)
    }

    @PostMapping("/{id}/member")
    fun addMember(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> {
        try {
            service.addMember(userId, id, memberId)
            return statusOf(HttpStatus.OK, "Conversation member is added")
        } catch (exception: IllegalAccessException) {
            return statusOf(HttpStatus.FORBIDDEN, exception.message!!)
        }
    }

    @DeleteMapping("/{id}/member")
    fun removeMember(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> {
        try {
            service.removeMember(userId, id, memberId)
            return statusOf(HttpStatus.OK, "Conversation member is removed")
        } catch (exception: IllegalAccessException) {
            return statusOf(HttpStatus.FORBIDDEN, exception.message!!)
        }
    }

    @PutMapping("/{id}/member/role")
    fun setMemberRole(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int,
        @RequestParam role: String
    ): ResponseEntity<StatusResponse<Nothing>> {
        try {
            service.setMemberRole(userId, id, memberId, role)
            return statusOf(HttpStatus.OK, "Conversation member role is set")
        } catch (exception: IllegalAccessException) {
            return statusOf(HttpStatus.FORBIDDEN, exception.message!!)
        }
    }
}