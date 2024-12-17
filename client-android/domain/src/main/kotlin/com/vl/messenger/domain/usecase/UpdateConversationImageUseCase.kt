package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.asByteArray
import com.vl.messenger.domain.boundary.FileStorageAccessor
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class UpdateConversationImageUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi,
    private val fileStorageAccessor: FileStorageAccessor
): SuspendedUseCase<UpdateConversationImageUseCase.Param, Unit> {

    override suspend fun invoke(param: Param): Unit =
        api.uploadConversationImage(
            sessionStore.getToken()!!.token,
            param.dialogId,
            fileStorageAccessor
                .readFile(param.imageUri)
                .asByteArray()
        )

    data class Param(val dialogId: String, val imageUri: String)
}