package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

internal class ProfileDto {
    val id: Int = 0
    val login: String = ""

    @SerializedName("image_url")
    val image: String? = null

    @SerializedName("is_hidden")
    val isHidden: Boolean = false
}