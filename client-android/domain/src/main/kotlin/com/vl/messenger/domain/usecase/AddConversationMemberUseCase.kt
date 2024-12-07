package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.User

class AddConversationMemberUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<AddConversationMemberUseCase.Param, Unit> {
    override suspend fun invoke(param: Param): Unit =
        api.addMemberToConversation(
            sessionStore.getToken()!!.token,
            param.dialog.id,
            param.user.id
        )

    data class Param(val dialog: Dialog, val user: User)
}