package com.vl.messenger.chat

import com.vl.messenger.chat.DtoMapper.toDto
import com.vl.messenger.chat.dto.MembersResponse
import com.vl.messenger.chat.dto.CreateConversationResponse
import com.vl.messenger.chat.dto.RoleResponse
import com.vl.messenger.chat.dto.RolesResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Provides interface to create and manage conversations.
 * Use chat controllers to exchange messages.
 * @see ChatRestController
 * @see ChatStompController
 */
@RestController
@RequestMapping("/conversations")
class ConversationController(
    @Value("\${base.url}") private val baseUrl: String,
    @Autowired private val service: ConversationService
) {

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

    @PutMapping("/{id}/set-name")
    fun setConversationName(
        @PathVariable("id") conversationId: Long,
        @RequestParam name: String
    ): ResponseEntity<StatusResponse<Nothing>> =
        when (service.setConversationName(userId, conversationId, name)) {
            ConversationService.CommonResult.SUCCESS ->
                statusOf(HttpStatus.OK, "Conversation name is set")

            ConversationService.CommonResult.NO_PRIVILEGE ->
                statusOf(HttpStatus.FORBIDDEN, "No edit conversation data privilege")
        }

    @PostMapping("/{id}/set-image", consumes = ["multipart/form-data"])
    fun setConversationImage(
        @RequestParam image: MultipartFile,
        @PathVariable("id") conversationId: Long
    ): ResponseEntity<StatusResponse<Nothing>> {
        if (image.contentType != "image/png" && image.contentType != "image/jpeg")
            return statusOf(HttpStatus.UNPROCESSABLE_ENTITY, "Only png and jpeg images are supported")

        return when (service.setConversationImage(userId, conversationId, image)) {
            ConversationService.CommonResult.SUCCESS ->
                statusOf(HttpStatus.OK, "Conversation image is set")

            ConversationService.CommonResult.NO_PRIVILEGE ->
                statusOf(HttpStatus.FORBIDDEN, "No edit conversation data privilege")
        }
    }

    @PutMapping("/{id}/leave")
    fun leaveConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<Nothing>> {
        service.leaveConversation(userId, id)
        return statusOf(HttpStatus.OK)
    }

    @GetMapping("/{id}/members")
    fun getMembers(
        @PathVariable("id") conversationId: Long,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<MembersResponse>> {
        return when (val result = service.getMembers(userId, conversationId, offset, limit)) {
            ConversationService.GetMembersResult.NoPrivilege ->
                statusOf(HttpStatus.FORBIDDEN, "No retrieve members privilege")

            ConversationService.GetMembersResult.NotFound ->
                statusOf(HttpStatus.NOT_FOUND, "No conversation")

            is ConversationService.GetMembersResult.Success ->
                statusOf(
                    payload = result.members.map {
                        MembersResponse.Member(it.id, it.login, "$baseUrl/${it.image}", it.role.toDto())
                    }.let(::MembersResponse)
                )
        }
    }

    @PutMapping("/{id}/members/{user_id}")
    fun addMember(
        @PathVariable("id") conversationId: Long,
        @PathVariable("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> =
        when (service.addMember(userId, conversationId, memberId)) {
            ConversationService.CommonResult.SUCCESS ->
                statusOf(HttpStatus.OK, "Conversation member is added")

            ConversationService.CommonResult.NO_PRIVILEGE ->
                statusOf(HttpStatus.FORBIDDEN, "No edit members privilege")
        }

    @DeleteMapping("/{id}/members/{user_id}")
    fun removeMember(
        @PathVariable("id") conversationId: Long,
        @PathVariable("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> =
        when (service.removeMember(userId, conversationId, memberId)) {
            ConversationService.CommonResult.SUCCESS ->
                statusOf(HttpStatus.OK, "Conversation member is removed")

            ConversationService.CommonResult.NO_PRIVILEGE ->
                statusOf(HttpStatus.FORBIDDEN, "No edit conversation members privilege")
        }

    @PutMapping("/{id}/members/{user_id}/role")
    fun setMemberRole(
        @PathVariable("id") conversationId: Long,
        @PathVariable("user_id") memberId: Int,
        @RequestParam role: Int
    ): ResponseEntity<StatusResponse<Nothing>> =
        when (service.setMemberRole(userId, conversationId, memberId, role)) {
            ConversationService.SetRoleResult.SUCCESS ->
                statusOf(HttpStatus.OK, "Conversation member role is set")

            ConversationService.SetRoleResult.NO_PRIVILEGE ->
                statusOf(HttpStatus.FORBIDDEN, "No edit conversation members rights privilege")

            ConversationService.SetRoleResult.ROLE_NOT_FOUND ->
                statusOf(HttpStatus.NOT_FOUND, "No such role")
        }

    @GetMapping("/{id}/roles") // in the future, roles can become specific for conversations
    fun getRoles(@PathVariable("id") conversationId: Long): ResponseEntity<StatusResponse<RolesResponse>> =
        when (val result = service.getRoles(userId, conversationId)) {
            ConversationService.CommonResultValue.NoPrivilege ->
                statusOf(HttpStatus.FORBIDDEN, "You're not participant")

            is ConversationService.CommonResultValue.Success ->
                statusOf(payload = result.value.toDto())
        }

    @GetMapping("/{id}/roles/mine")
    fun getOwnRole(@PathVariable("id") conversationId: Long): ResponseEntity<StatusResponse<RoleResponse>> =
        when (val result = service.getRole(userId, conversationId)) {
            ConversationService.CommonResultValue.NoPrivilege ->
                statusOf(HttpStatus.FORBIDDEN, "You're not participant")

            is ConversationService.CommonResultValue.Success ->
                statusOf(payload = RoleResponse(result.value.toDto()))
        }

    @GetMapping("/{id}/report", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun generateReport(@PathVariable("id") conversationId: Long): ResponseEntity<ByteArray> =
        when (val result = service.generateReport(userId, conversationId)) {
            ConversationService.GenerateReportResult.NoPrivilege ->
                ResponseEntity(HttpStatus.FORBIDDEN)

            is ConversationService.GenerateReportResult.Success ->
                ResponseEntity(result.reportFile, HttpStatus.OK)
        }
}