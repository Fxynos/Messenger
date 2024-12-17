package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.VerboseUser

class GetUserByIdUseCase(
    private val sessionStore: SessionStore,
    private val messengerRestApi: MessengerRestApi
): SuspendedUseCase<Int, VerboseUser> {
    override suspend fun invoke(param: Int): VerboseUser =
        messengerRestApi.getUserById(sessionStore.getToken()!!.token, param)
}