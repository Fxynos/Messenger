package com.vl.messenger.ui

import android.graphics.Bitmap
import com.vl.messenger.domain.entity.ExtendedDialog
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.adapter.MessagePagingAdapter
import com.vl.messenger.ui.entity.DialogUi
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UiMapper {
    fun Bitmap.toDomain() = ByteArrayOutputStream().also {
        compress(Bitmap.CompressFormat.PNG, 100, it)
    }.toByteArray()

    fun Message.toUi(loggedUserId: Int, sender: User) = when {
        senderId == loggedUserId -> MessagePagingAdapter.MessageItem.Sent(
            id = id,
            text = content,
            time = unixSec.toTimestamp("HH:mm")
        )
        else -> MessagePagingAdapter.MessageItem.Received(
            id = id,
            text = content,
            time = unixSec.toTimestamp("HH:mm"),
            senderName = sender.login,
            senderImageUrl = sender.imageUrl
        )
    }
    fun ExtendedDialog.toUi() = DialogUi(
        id = dialog.id,
        isPrivate = dialog.isPrivate,
        name = dialog.title,
        imageUrl = dialog.image,
        lastMessageText = lastMessage?.content,
        lastMessageSenderName = sender?.login
    )

    private fun Long.toTimestamp(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())
        .format(Date(this * 1000))
}