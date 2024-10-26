package com.vl.messenger.data

import android.content.Context
import android.net.Uri
import com.vl.messenger.domain.boundary.FileStorageAccessor
import java.io.InputStream

class FileStorageAccessorImpl(private val context: Context): FileStorageAccessor {
    override fun readFile(uri: String): InputStream =
        context.contentResolver.openInputStream(
            Uri.parse(uri)
        )!!
}