package com.vl.messenger.ui

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object UiMapper {
    fun Bitmap.toDomain() = ByteArrayOutputStream().also {
        compress(Bitmap.CompressFormat.PNG, 100, it)
    }.toByteArray()
}