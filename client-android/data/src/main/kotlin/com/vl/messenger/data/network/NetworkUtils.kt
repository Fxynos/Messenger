package com.vl.messenger.data.network

internal fun String.toBearerAuthHeader() = "Bearer $this"