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
@RequestMapping("/conversation")
class ConversationController(@Autowired private val service: ConversationService) {
    @PostMapping
    fun createConversation(
        @NotBlank @RequestParam name: String
    ): ResponseEntity<StatusResponse<CreateConversationResponse>> {
        if (name.isBlank())
            return statusOf(HttpStatus.BAD_REQUEST, "name is blank")
        if (name.length > 20)
            return statusOf(HttpStatus.BAD_REQUEST, "name is too long")
        return statusOf(payload = CreateConversationResponse(service.createConversation(userId, name)))
    }

    @GetMapping("/{id}")
    fun describeConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<DescribeConversationResponse>> {
        TODO()
    }

    @PutMapping("/{id}")
    fun updateConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<Nothing>> {
        TODO()
    }

    @PostMapping("/{id}/member")
    fun addMember(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> {
        TODO()
    }

    @DeleteMapping("/{id}/member")
    fun removeMember(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> {
        TODO()
    }

    @PutMapping("/{id}/member/role")
    fun setRole(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int,
        @RequestParam role: String
    ): ResponseEntity<StatusResponse<Nothing>> {
        TODO()
    }
}