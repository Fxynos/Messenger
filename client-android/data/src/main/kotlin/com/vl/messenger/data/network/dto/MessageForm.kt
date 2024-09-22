package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class MessageForm(
    @SerializedName("user_id")
    val userId: Int,
    val content: String
)