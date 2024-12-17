package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class LeaveConversationUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<String, Unit> {
    /**
     * @param param dialog id
     */
    override suspend fun invoke(param: String): Unit =
        api.leaveConversation(sessionStore.getToken()!!.token, param)
}