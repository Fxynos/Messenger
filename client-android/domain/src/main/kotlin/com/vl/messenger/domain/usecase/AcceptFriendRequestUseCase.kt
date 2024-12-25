package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class AcceptFriendRequestUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi
): SuspendedUseCase<Long, Unit> {
    /**
     * @param param notification id
     */
    override suspend fun invoke(param: Long): Unit =
        api.acceptFriendRequest(sessionStore.getToken()!!.token, param)
}