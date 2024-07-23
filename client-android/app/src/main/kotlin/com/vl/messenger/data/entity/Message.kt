package com.vl.messenger.data.entity

import android.os.Parcel
import android.os.Parcelable

class Message(
    val id: Long,
    val senderId: Int,
    val timestamp: Long,
    val content: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(senderId)
        parcel.writeLong(timestamp)
        parcel.writeString(content)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}