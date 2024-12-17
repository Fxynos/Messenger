package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Message

class SendMessageUseCase(
    private val messengerApi: MessengerRestApi,
    private val sessionStore: SessionStore
): SuspendedUseCase<SendMessageUseCase.Param, Message> {

    override suspend fun invoke(param: Param): Message =
        messengerApi.sendMessage(sessionStore.getToken()!!.token, param.message, param.dialogId)

    data class Param(val message: String, val dialogId: String)
}