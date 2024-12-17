package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore

class UpdateProfileHiddenUseCase(
    val sessionStore: SessionStore,
    val api: MessengerRestApi
): SuspendedUseCase<Boolean, Unit> {
    /**
     * @param param is hidden
     */
    override suspend fun invoke(param: Boolean) =
        api.setProfileHidden(sessionStore.getToken()!!.token, param)
}