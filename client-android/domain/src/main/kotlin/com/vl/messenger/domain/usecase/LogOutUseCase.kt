package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.SessionStore

class LogOutUseCase(
    private val sessionStore: SessionStore
): SuspendedUseCase<Unit, Unit> {
    override suspend fun invoke(param: Unit) {
        sessionStore.removeToken()
    }
}