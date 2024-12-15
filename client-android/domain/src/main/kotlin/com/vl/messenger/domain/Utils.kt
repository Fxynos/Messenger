package com.vl.messenger.domain

import java.io.ByteArrayOutputStream
import java.io.InputStream

internal fun InputStream.asByteArray(): ByteArray =
    ByteArrayOutputStream().apply {
        val bytes = ByteArray(4096)
        while (available() > 0)
            write(bytes, 0, read(bytes))
    }.toByteArray()