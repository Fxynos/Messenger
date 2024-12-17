package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class UserDto {
    val id: Int = 0
    val login: String = ""

    @SerializedName("image_url")
    val image: String? = null

    @SerializedName("friend_status")
    val friendStatus: FriendStatusDto? = null
}