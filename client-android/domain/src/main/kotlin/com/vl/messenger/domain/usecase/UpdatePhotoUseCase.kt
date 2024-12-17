package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.asByteArray
import com.vl.messenger.domain.boundary.FileStorageAccessor
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

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