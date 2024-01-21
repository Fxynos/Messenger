package com.vl.messenger.profile

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationService(@Autowired private val dataMapper: DataMapper) {

    fun addNotification(userId: Int, title: String, content: String) {
        dataMapper.addNotification(userId, title, content)
        // TODO deliver to client
    }

    fun getNotifications(userId: Int, fromId: Long?, limit: Int) =
        dataMapper.getNotifications(userId, fromId, limit)

    fun markAsSeen(notificationId: Long) = dataMapper.markNotificationAsSeen(notificationId)

    fun hasNotification(userId: Int, notificationId: Long) = dataMapper.hasNotification(userId, notificationId)

    fun deliverNotification(notification: DataMapper.Notification) {
        TODO()
    }
}