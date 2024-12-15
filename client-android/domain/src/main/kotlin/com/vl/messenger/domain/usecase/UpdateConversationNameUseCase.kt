package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class UpdateConversationNameUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<UpdateConversationNameUseCase.Param, Unit> {

    override suspend fun invoke(param: Param): Unit =
        api.setConversationName(sessionStore.getToken()!!.token, param.dialogId, param.name)

    data class Param(val dialogId: String, val name: String)
}