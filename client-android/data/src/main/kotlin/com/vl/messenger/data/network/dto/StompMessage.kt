package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

class StompMessage {
    var id: Long? = null
    @field:SerializedName("dialog_id")
    var dialogId: String? = null
    @field:SerializedName("sender_id")
    var senderId: Int? = null
    lateinit var content: String
}