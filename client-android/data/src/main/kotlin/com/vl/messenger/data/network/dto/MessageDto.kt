package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class MessageDto {
    val id: Long = 0
    @SerializedName("sender_id")
    val senderId: Int = 0
    val timestamp: Long = 0
    val content: String = ""
}