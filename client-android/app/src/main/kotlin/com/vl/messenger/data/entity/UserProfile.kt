package com.vl.messenger.data.entity

class UserProfile(
    id: Int, login: String, imageUrl: String?,
    val friendStatus: FriendStatus
): User(id, login, imageUrl) {
    override fun equals(other: Any?) = other
            .takeIf { it is UserProfile }
            ?.let {
                val profile = it as UserProfile
                id == profile.id && friendStatus == profile.friendStatus
            } ?: false // compare by id and friend status
}