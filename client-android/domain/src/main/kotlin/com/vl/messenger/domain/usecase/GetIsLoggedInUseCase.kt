package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.SessionStore

class GetIsLoggedInUseCase(
    private val sessionStore: SessionStore
): SuspendedUseCase<Unit, Boolean> {
    override suspend fun invoke(param: Unit): Boolean = sessionStore.getToken() != null
}