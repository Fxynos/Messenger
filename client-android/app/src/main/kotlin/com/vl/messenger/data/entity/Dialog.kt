package com.vl.messenger.data.entity

import android.os.Parcel
import android.os.Parcelable

data class Dialog(
    val id: Long,
    val isPrivate: Boolean,
    val title: String,
    val image: String?,
    val lastMessage: Message?,
    val lastMessageSender: User?
): Parcelable { // TODO use parcelize plugin for default implementation
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readParcelable(Message::class.java.classLoader),
        parcel.readParcelable(User::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeByte(if (isPrivate) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeParcelable(lastMessage, flags)
        parcel.writeParcelable(lastMessageSender, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Dialog> {
        override fun createFromParcel(parcel: Parcel): Dialog {
            return Dialog(parcel)
        }

        override fun newArray(size: Int): Array<Dialog?> {
            return arrayOfNulls(size)
        }
    }
}