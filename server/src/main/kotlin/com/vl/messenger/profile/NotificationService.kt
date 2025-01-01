package com.vl.messenger.profile

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationService(@Autowired private val dataMapper: DataMapper) {

    fun sendInfoNotification(userId: Int, title: String, content: String): Unit =
        deliverNotification(
            dataMapper.addNotification(userId, title, content)
        )

    fun sendFriendInviteNotification(userId: Int, friendId: Int): Unit =
        deliverNotification(
            dataMapper.addFriendInviteNotification(userId, friendId)
        )

    fun sendConversationInviteNotification(userId: Int, memberId: Int, conversationId: Long): Unit =
        deliverNotification(
            dataMapper.addConversationInvite(userId, memberId, conversationId)
        )

    fun getNotifications(userId: Int, fromId: Long?, limit: Int) =
        dataMapper.getNotifications(userId, fromId, limit)

    fun markAsSeen(notificationId: Long) = dataMapper.markNotificationAsSeen(notificationId)

    fun hasNotification(userId: Int, notificationId: Long) = dataMapper.hasNotification(userId, notificationId)

    private fun deliverNotification(notificationId: Long) {
        // TODO implement FCM
    }
}