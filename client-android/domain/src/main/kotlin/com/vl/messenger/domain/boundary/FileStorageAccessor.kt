package com.vl.messenger.domain.boundary

import java.io.InputStream

interface FileStorageAccessor {
    fun readFile(uri: String): InputStream
}