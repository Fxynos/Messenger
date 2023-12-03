package com.vl.messenger.user

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class SocialService(@Autowired private val dataMapper: DataMapper) {

    private val logger = Logger.getLogger("SocialService")

    fun sendFriendRequest(userId: Int, friendId: Int) {
        try {
            dataMapper.sendFriendRequest(userId, friendId)
        } catch (e: IllegalStateException) {
            logger.warning("User#$userId attempts to send friend request, but it is already sent")
        }
    }

    fun hasRequestFrom(userId: Int, senderId: Int) =
        dataMapper.getFriendRequestId(senderId, userId) != null
}