package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class TokenDto {
    @SerializedName("user_id")
    val userId: Int = 0
    @SerializedName("access_token")
    val token: String = ""
    @SerializedName("expires_in")
    val expirationSec: Long = 0
}