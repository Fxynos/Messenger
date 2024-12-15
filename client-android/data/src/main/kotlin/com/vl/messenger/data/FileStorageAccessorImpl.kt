package com.vl.messenger.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.vl.messenger.domain.boundary.FileStorageAccessor
import java.io.InputStream
import java.io.OutputStream

class FileStorageAccessorImpl(context: Context): FileStorageAccessor {

    private val contentResolver = context.contentResolver

    override fun readFile(uri: String): InputStream =
        contentResolver.openInputStream(
            Uri.parse(uri)
        )!!

    override fun createFileAtDownloads(directory: String, basename: String, mimeType: String): OutputStream =
        contentResolver.openOutputStream(
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$directory")
                put(MediaStore.Downloads.DISPLAY_NAME, basename)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
            })!!
        )!!
}