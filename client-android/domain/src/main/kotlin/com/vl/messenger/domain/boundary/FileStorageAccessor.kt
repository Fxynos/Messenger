package com.vl.messenger.domain.boundary

import java.io.InputStream
import java.io.OutputStream

interface FileStorageAccessor {
    fun readFile(uri: String): InputStream
    fun createFileAtDownloads(directory: String, basename: String, mimeType: String): OutputStream
}