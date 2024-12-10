package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

class ConversationMemberDto {
    @SerializedName("user_id")
    val userId: Int = 0
    val login: String = ""
    val image: String? = null
    lateinit var role: RoleDto
}