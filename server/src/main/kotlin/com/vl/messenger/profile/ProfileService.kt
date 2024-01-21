package com.vl.messenger.profile

import com.vl.messenger.DataMapper
import com.vl.messenger.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

@Service
class ProfileService(
    @Autowired private val dataMapper: DataMapper,
    @Autowired private val storageService: StorageService,
    @Autowired private val notificationService: NotificationService
) {

    private val logger = Logger.getLogger("SocialService")

    fun setProfileImage(userId: Int, image: MultipartFile) {
        val path = storageService.saveUserImage(image, userId)
        dataMapper.setProfileImage(userId, path)
    }

    fun getUser(userId: Int) = dataMapper.getVerboseUser(userId)

    fun searchUsers(login: String, fromId: Int?, limit: Int) = dataMapper.getUsersByLogin(login, fromId, limit)

    fun hasRequestFrom(userId: Int, senderId: Int) =
        dataMapper.getFriendRequestId(senderId, userId) != null

    fun isFriend(userId: Int, friendId: Int) = dataMapper.areFriends(userId, friendId)

    /**
     * Send friend request or accept existing
     * @return true if users are friends now, false if there's no answering request from the second one
     */
    fun addFriend(userId: Int, friendId: Int): Boolean {
        dataMapper.getFriendRequestId(friendId, userId)?.let { requestId -> // accept existing friend request if received
            dataMapper.addFriend(userId, friendId)
            removeFriendRequest(requestId)
            notificationService.addNotification(
                friendId,
                "Новый друг",
                "Пользователь ${dataMapper.getVerboseUser(userId)!!.login} принял ваш запрос в друзья"
            )
            return true
        }
        try {
            dataMapper.addFriendRequest(userId, friendId)
        } catch (e: IllegalStateException) {
            logger.warning("User#$userId attempts to send friend request, but it is already sent")
        }
        return false
    }

    fun getFriends(userId: Int) = dataMapper.getFriends(userId)

    /**
     * Revoke own friend request or remove from friends, also can be used to reject inbound request
     * @return true if friend is removed, false if request is just revoked, also false if it had no effect
     */
    fun removeFriend(userId: Int, friendId: Int): Boolean {
        if (dataMapper.areFriends(userId, friendId)) {
            dataMapper.deleteFriend(userId, friendId)
            notificationService.addNotification(
                friendId,
                "Друзья",
                "Пользователь ${dataMapper.getVerboseUser(userId)!!.login} удалил вас из друзей"
            )
            return true
        }
        dataMapper.getFriendRequestId(friendId, userId)?.also { // reject inbound request
            removeFriendRequest(it)
            notificationService.addNotification(
                friendId,
                "Друзья",
                "Пользователь ${dataMapper.getVerboseUser(userId)!!.login} отклонил ваш запрос в друзья"
            )
            return false
        }
        dataMapper.getFriendRequestId(userId, friendId)?.also(::removeFriendRequest) // revoke outbound request
            ?: logger.warning("User #$userId attempts to revoke friend request not existing")
        return false
    }

    /**
     * @return false if user was already in blacklist, true otherwise
     */
    fun addToBlacklist(userId: Int, blockedId: Int): Boolean {
        if (isBlocked(userId, blockedId))
            return false
        dataMapper.addToBlacklist(userId, blockedId)
        return true
    }

    fun getBlacklist(userId: Int) = dataMapper.getBlacklist(userId)

    /**
     * @return false if user wasn't blocked, true otherwise
     */
    fun removeFromBlacklist(userId: Int, blockedId: Int): Boolean {
        if (!isBlocked(userId, blockedId))
            return false
        dataMapper.removeFromBlacklist(userId, blockedId)
        return true
    }

    /**
     * Blacklisted
     */
    private fun isBlocked(userId: Int, blockedId: Int) = dataMapper.isInBlacklist(userId, blockedId)

    private fun removeFriendRequest(notificationId: Long) = dataMapper.removeNotification(notificationId)
}