package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Profile
import com.vl.messenger.domain.entity.User

class GetLoggedUserProfileUseCase(
    private val sessionStore: SessionStore,
    private val messengerRestApi: MessengerRestApi
): SuspendedUseCase<Unit, Profile> {
    override suspend fun invoke(param: Unit): Profile =
        messengerRestApi.getProfile(sessionStore.getToken()!!.token)
}