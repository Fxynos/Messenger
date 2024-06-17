package com.vl.messenger.data.entity

import android.os.Parcel
import android.os.Parcelable

class PrivateDialog(private val companion: User): Dialog, Parcelable {
    override val id: Int get() = companion.id
    override val name: String get() = companion.login
    override val image: String? get() = companion.imageUrl

    constructor(parcel: Parcel) : this(parcel.readParcelable<User>(User::class.java.classLoader)!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(companion, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<PrivateDialog> {
        override fun createFromParcel(parcel: Parcel): PrivateDialog {
            return PrivateDialog(parcel)
        }

        override fun newArray(size: Int): Array<PrivateDialog?> {
            return arrayOfNulls(size)
        }
    }
}