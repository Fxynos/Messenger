package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.usecase.param.CredentialsParam

class SignInUseCase(
    private val messengerApi: MessengerRestApi,
    private val sessionStore: SessionStore
): SuspendedUseCase<CredentialsParam, SignInUseCase.Result> {

    override suspend fun invoke(param: CredentialsParam) =
        when (val result = messengerApi.signIn(param.login, param.password)) {
            is MessengerRestApi.SignInResult.Success -> {
                sessionStore.setToken(result.token)
                Result.Success
            }
            MessengerRestApi.SignInResult.WrongCredentials -> Result.WrongCredentials
            is MessengerRestApi.SignInResult.Error -> Result.Error(result.throwable)
        }

    sealed interface Result {
        data object Success: Result
        data object WrongCredentials: Result
        data class Error(val cause: Throwable): Result
    }
}