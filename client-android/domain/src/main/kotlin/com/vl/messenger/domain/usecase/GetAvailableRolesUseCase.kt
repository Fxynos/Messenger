package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Role

class GetAvailableRolesUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<String, List<Role>> {
    /**
     * @param param dialog id of conversation
     */
    override suspend fun invoke(param: String): List<Role> =
        api.getConversationRoles(sessionStore.getToken()!!.token, param)
}