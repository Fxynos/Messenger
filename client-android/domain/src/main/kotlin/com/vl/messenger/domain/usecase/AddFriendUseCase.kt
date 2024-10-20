package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class AddFriendUseCase(
    private val sessionStore: SessionStore,
    private val messengerRestApi: MessengerRestApi
): SuspendedUseCase<Int, Unit> {
    /**
     * @param param user id
     */
    override suspend fun invoke(param: Int) {
        messengerRestApi.addFriend(sessionStore.getToken()!!.token, param)
    }
}