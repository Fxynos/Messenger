package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Role

class GetOwnConversationRoleUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<String, Role> {
    /**
     * @param param dialog id of conversation
     */
    override suspend fun invoke(param: String): Role =
        api.getOwnConversationRole(sessionStore.getToken()!!.token, param)
}