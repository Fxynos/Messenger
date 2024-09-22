package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.usecase.param.DialogParam
import kotlinx.coroutines.runBlocking

class SendMessageUseCase(
    private val messengerApi: MessengerRestApi,
    private val sessionStore: SessionStore
): SuspendedUseCase<SendMessageUseCase.Param, Message> {

    private val token: String get() = runBlocking { sessionStore.getToken()!!.token }

    override suspend fun invoke(param: Param): Message = when (param.dialog) {
        is DialogParam.PrivateDialog ->
            messengerApi.sendMessage(token, param.message, param.dialog.userId)
        is DialogParam.Conversation ->
            messengerApi.sendMessage(token, param.message, param.dialog.conversationId)
    }

    data class Param(val message: String, val dialog: DialogParam)
}