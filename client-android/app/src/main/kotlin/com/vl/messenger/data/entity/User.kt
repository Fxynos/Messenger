package com.vl.messenger.data.entity

import android.os.Parcel
import android.os.Parcelable

open class User(
    val id: Int,
    val login: String,
    val imageUrl: String?
): Parcelable {
    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeInt(id)
            writeString(login)
            writeString(imageUrl)
        }
    }

    override fun equals(other: Any?) =
        other.takeIf { it is User }?.let { (it as User).id == id } ?: false // compare by id

    companion object CREATOR: Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel) = parcel.run {
            User(readInt(), readString()!!, readString())
        }

        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}