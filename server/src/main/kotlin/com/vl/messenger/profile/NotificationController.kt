package com.vl.messenger.profile

import com.vl.messenger.DataMapper
import com.vl.messenger.chat.dto.ConversationResponse
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.profile.dto.NotificationsResponse
import com.vl.messenger.statusOf
import com.vl.messenger.toDto
import com.vl.messenger.userId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/notifications")
@RestController
class NotificationController(
    @Value("\${base.url}") private val baseUrl: String,
    @Autowired private val service: NotificationService
) {
    @GetMapping("/")
    fun getNotifications(
        @RequestParam("from_id", required = false) fromId: Long?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<StatusResponse<NotificationsResponse>> {
        return statusOf(payload = NotificationsResponse(service.getNotifications(userId, fromId, limit).map {
            when (it) {
                is DataMapper.PlainNotification -> NotificationsResponse.Notification(
                    it.id,
                    it.unixSec,
                    it.isSeen,
                    it.title,
                    it.content
                )
                is DataMapper.FriendRequest -> NotificationsResponse.Notification(
                    it.id,
                    it.unixSec,
                    it.isSeen,
                    it.sender.toDto(baseUrl)
                )
                is DataMapper.ConversationRequest -> NotificationsResponse.Notification(
                    it.id,
                    it.unixSec,
                    it.isSeen,
                    it.sender.toDto(baseUrl),
                    ConversationResponse(
                        it.conversation.id,
                        it.conversation.name,
                        "$baseUrl/${it.conversation.image}"
                    )
                )
            }
        }))
    }

    @PutMapping("/mark-as-seen")
    fun markAsSeen(@RequestParam id: Long): ResponseEntity<StatusResponse<Nothing>> { // TODO mark several at once
        if (!service.hasNotification(userId, id))
            return statusOf(HttpStatus.GONE, "No such notification")
        service.markAsSeen(id)
        return statusOf(reason = "Marked as seen")
    }
}