package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.User

class GetFriendsUseCase(
    private val sessionStore: SessionStore,
    private val messengerRestApi: MessengerRestApi
): SuspendedUseCase<Unit, List<User>> {
    override suspend fun invoke(param: Unit): List<User> =
        messengerRestApi.getFriends(sessionStore.getToken()!!.token)
}