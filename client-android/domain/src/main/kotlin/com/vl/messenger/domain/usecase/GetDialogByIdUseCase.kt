package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Dialog

class GetDialogByIdUseCase(
    private val sessionStore: SessionStore,
    private val messengerRestApi: MessengerRestApi
): SuspendedUseCase<String, Dialog> {
    /**
     * @param param dialog id
     */
    override suspend fun invoke(param: String): Dialog =
        messengerRestApi.getDialog(sessionStore.getToken()!!.token, param)
}
