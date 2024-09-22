package com.vl.messenger.ui.entity

import android.net.Uri

sealed interface MessageUi {
    val id: Long
    val message: String
    val time: String

    data class Sent(
        override val id: Long,
        override val message: String,
        override val time: String
    ): MessageUi

    sealed interface Received: MessageUi {

        data class Unsigned(
            override val id: Long,
            override val message: String,
            override val time: String
        ): MessageUi

        data class Signed(
            override val id: Long,
            override val message: String,
            override val time: String,
            val name: String,
            val imageUrl: Uri
        ): MessageUi
    }
}