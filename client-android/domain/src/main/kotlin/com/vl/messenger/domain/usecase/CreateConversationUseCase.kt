package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class CreateConversationUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<String, String> {
    /**
     * @param param conversation name
     */
    override suspend fun invoke(param: String): String =
        api.createConversation(sessionStore.getToken()!!.token, param)
}