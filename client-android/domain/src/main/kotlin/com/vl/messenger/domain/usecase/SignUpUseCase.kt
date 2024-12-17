package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.usecase.param.CredentialsParam

class SignUpUseCase(
    private val messengerApi: MessengerRestApi,
    private val signInUseCase: SignInUseCase
): SuspendedUseCase<CredentialsParam, SignUpUseCase.Result> {

    override suspend fun invoke(param: CredentialsParam) =
        when (val result = messengerApi.signUp(param.login, param.password)) {

            MessengerRestApi.SignUpResult.Success ->
                when (val signInResult = signInUseCase(param)) {
                    is SignInUseCase.Result.Error ->
                        Result.Error(signInResult.cause)

                    SignInUseCase.Result.Success ->
                        Result.Success

                    SignInUseCase.Result.WrongCredentials ->
                        Result.Error(RuntimeException("Wrong credentials"))
                }

            MessengerRestApi.SignUpResult.LoginIsTaken -> Result.LoginTaken

            is MessengerRestApi.SignUpResult.Error -> Result.Error(result.throwable)
        }

    sealed interface Result {
        data object Success: Result
        data object LoginTaken: Result
        data class Error(val cause: Throwable): Result
    }
}