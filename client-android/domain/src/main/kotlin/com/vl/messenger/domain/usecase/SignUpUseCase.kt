package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.usecase.param.CredentialsParam

class SignUpUseCase(
    private val messengerApi: MessengerRestApi,
    private val signInUseCase: SignInUseCase
): SuspendedUseCase<CredentialsParam, SignUpUseCase.Result> {

    override suspend fun invoke(param: CredentialsParam) =
        when (messengerApi.signUp(param.login, param.password)) {

            MessengerRestApi.SignUpResult.Success ->
                if (signInUseCase(param) == SignInUseCase.Result.SUCCESS)
                    Result.SUCCESS
                else
                    Result.ERROR

            MessengerRestApi.SignUpResult.LoginIsTaken -> Result.LOGIN_TAKEN

            is MessengerRestApi.SignUpResult.Error -> Result.ERROR
        }

    enum class Result {
        SUCCESS,
        LOGIN_TAKEN,
        ERROR
    }
}