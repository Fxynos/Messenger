package com.vl.messenger.user

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class SocialService(@Autowired private val dataMapper: DataMapper) {

    private val logger = Logger.getLogger("SocialService")

    fun searchUsers(login: String) = dataMapper.getUsersByLogin(login)

    fun hasRequestFrom(userId: Int, senderId: Int) =
        dataMapper.getFriendRequestId(senderId, userId) != null

    /**
     * Send friend request or accept existing
     * @return true if users are friends now, false if there's no answering request from the second one
     */
    fun addFriend(userId: Int, friendId: Int): Boolean {
        dataMapper.getFriendRequestId(friendId, userId)?.let { requestId -> // accept existing friend request if received
            dataMapper.addFriend(userId, friendId)
            dataMapper.removeNotification(requestId) // request id references notification id
            return true
        }
        val ms = System.currentTimeMillis()
        try {
            dataMapper.sendFriendRequest(userId, friendId)
        } catch (e: IllegalStateException) {
            logger.warning("User#$userId attempts to send friend request, but it is already sent") // TODO log also suspicious redundant adding friend
        }
        logger.finest("Sending friend request took ${System.currentTimeMillis() - ms} ms") // TODO remove after tests
        return false
    }

    fun getFriends(userId: Int) = dataMapper.getFriends(userId)

    /**
     * Revoke own friend request or remove from friends
     * @return true if friend is removed, false if request is just revoked, also false if it had no effect
     */
    fun removeFriend(userId: Int, friendId: Int): Boolean {
        if (dataMapper.areFriends(userId, friendId)) {
            dataMapper.deleteFriend(userId, friendId)
            return true
        }
        dataMapper.getFriendRequestId(userId, friendId)?.also {
            dataMapper.removeNotification(it)
        } ?: logger.warning("User #$userId attempts to revoke friend request not existing")
        return false
    }
}