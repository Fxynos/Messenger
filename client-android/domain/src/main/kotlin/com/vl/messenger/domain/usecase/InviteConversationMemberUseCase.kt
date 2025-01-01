package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class InviteConversationMemberUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<InviteConversationMemberUseCase.Param, Unit> {
    override suspend fun invoke(param: Param): Unit =
        api.inviteMemberToConversation(
            sessionStore.getToken()!!.token,
            param.dialogId,
            param.userId
        )

    data class Param(val dialogId: String, val userId: Int)
}