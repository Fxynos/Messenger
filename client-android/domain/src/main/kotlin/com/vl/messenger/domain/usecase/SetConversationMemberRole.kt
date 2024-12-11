package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class SetConversationMemberRole(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<SetConversationMemberRole.Param, Unit> {

    override suspend fun invoke(param: Param) =
        api.setConversationMemberRole(
            sessionStore.getToken()!!.token,
            param.dialogId,
            param.userId,
            param.roleId
        )

    data class Param(val dialogId: String, val userId: Int, val roleId: Int)
}