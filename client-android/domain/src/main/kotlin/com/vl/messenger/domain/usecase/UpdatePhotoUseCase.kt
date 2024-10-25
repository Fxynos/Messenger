package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.FileStorageAccessor
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UpdatePhotoUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi,
    private val fileStorageAccessor: FileStorageAccessor
): SuspendedUseCase<String, Unit> {
    /**
     * @param param uri to local file
     */
    override suspend fun invoke(param: String) {
        api.uploadPhoto(
            sessionStore.getToken()!!.token,
            fileStorageAccessor
                .readFile(param)
                .asByteArray()
        )
    }
}

private fun InputStream.asByteArray(): ByteArray =
    ByteArrayOutputStream().apply {
        val bytes = ByteArray(4096)
        while (available() > 0)
            write(bytes, 0, read(bytes))
    }.toByteArray()