package com.vl.messenger.chat

import com.vl.messenger.chat.dto.CreateConversationResponse
import com.vl.messenger.chat.dto.ConversationResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import com.vl.messenger.userId
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

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

    @GetMapping("/{id}")
    fun describeConversation(@PathVariable id: Long): ResponseEntity<StatusResponse<ConversationResponse>> {
        if (!service.isMember(userId, id))
            return statusOf(HttpStatus.GONE, "No conversation or you are not its member")
        val conversation = service.getConversation(id)!!
        return statusOf(payload = ConversationResponse(
            conversation.id,
            conversation.name,
            if (conversation.image == null) null else "$baseUrl/${conversation.image}",
            service.getMembers(id).map {
                ConversationResponse.Member(it.id, it.login, "$baseUrl/${it.image}", it.role.name)
            }
        ))
    }

    @PutMapping("/{id}/set-name")
    fun setConversationName(
        @PathVariable id: Long,
        @RequestParam name: String
    ): ResponseEntity<StatusResponse<Nothing>> {
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.EDIT_DATA))
            return statusOf(HttpStatus.FORBIDDEN, "No edit conversation data privilege")
        service.setConversationName(id, name)
        return statusOf(HttpStatus.OK, "Conversation name is set")
    }

    @PostMapping("/{id}/set-image", consumes = ["multipart/form-data"])
    fun setConversationImage(
        @RequestParam image: MultipartFile,
        @PathVariable id: Long
    ): ResponseEntity<StatusResponse<Nothing>> {
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.EDIT_DATA))
            return statusOf(HttpStatus.FORBIDDEN, "No edit conversation data privilege")
        if (image.contentType != "image/png" && image.contentType != "image/jpeg")
            return statusOf(HttpStatus.UNPROCESSABLE_ENTITY, "Only png and jpeg images are supported")
        service.setConversationImage(id, image)
        return statusOf(HttpStatus.OK, "Conversation image is set")
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
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.EDIT_MEMBERS))
            return statusOf(HttpStatus.FORBIDDEN, "No edit members privilege")
        service.addMember(userId, id, memberId)
        return statusOf(HttpStatus.OK, "Conversation member is added")
    }

    @DeleteMapping("/{id}/member")
    fun removeMember(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int
    ): ResponseEntity<StatusResponse<Nothing>> {
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.EDIT_MEMBERS))
            return statusOf(HttpStatus.FORBIDDEN, "No edit conversation members privilege")
        service.removeMember(userId, id, memberId)
        return statusOf(HttpStatus.OK, "Conversation member is removed")
    }

    @PutMapping("/{id}/member/role")
    fun setMemberRole(
        @PathVariable id: Long,
        @RequestParam("user_id") memberId: Int,
        @RequestParam role: String
    ): ResponseEntity<StatusResponse<Nothing>> {
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.EDIT_RIGHTS))
            return statusOf(HttpStatus.FORBIDDEN, "No edit conversation members rights privilege")
        service.setMemberRole(id, memberId, role)
        return statusOf(HttpStatus.OK, "Conversation member role is set")
    }

    @GetMapping("/{id}/report", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun generateReport(@PathVariable id: Long): ResponseEntity<ByteArray> {
        if (!service.hasPrivilege(userId, id, ConversationService.Privilege.GET_REPORTS))
            return ResponseEntity(HttpStatus.FORBIDDEN)
        ByteArrayOutputStream().use { outputStream ->
            service.generateReport(id, outputStream)
            return ResponseEntity(outputStream.toByteArray(), HttpStatus.OK) // TODO large report can lead to out of memory error
        }
    }
}