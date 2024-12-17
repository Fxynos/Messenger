package com.vl.messenger.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.usecase.GetIsLoggedInUseCase
import com.vl.messenger.domain.usecase.SignInUseCase
import com.vl.messenger.domain.usecase.SignUpUseCase
import com.vl.messenger.domain.usecase.param.CredentialsParam
import com.vl.messenger.ui.utils.launch
import com.vl.messenger.ui.utils.launchHeavy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application,
    getIsLoggedInUseCase: GetIsLoggedInUseCase,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(UiState.REGULAR)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    init {
        launch {
            if (getIsLoggedInUseCase(Unit))
                _events.emit(DataDrivenEvent.NavigateLoggedIn)
        }
    }

    fun navigateToSignIn() {
        if (uiState.value != UiState.LOADING)
            emitEvent(DataDrivenEvent.NavigateSignIn)
    }

    fun navigateToSignUp() {
        if (uiState.value != UiState.LOADING)
            emitEvent(DataDrivenEvent.NavigateSignUp)
    }

    fun signIn(login: String, password: String) {
        updateState(UiState.LOADING)
        if (validate(login, password))
            launchHeavy {
                when (val result = signInUseCase(CredentialsParam(login, password))) {
                    SignInUseCase.Result.Success -> emitEvent(DataDrivenEvent.NavigateLoggedIn)
                    SignInUseCase.Result.WrongCredentials -> emitEvent(DataDrivenEvent.NotifyWrongCredentials)
                    is SignInUseCase.Result.Error -> {
                        Log.w(TAG, result.cause)
                        emitEvent(DataDrivenEvent.NotifyError(null))
                    }
                }
                updateState(UiState.REGULAR)
            }
    }

    fun signUp(login: String, password: String, repeatPassword: String) {
        updateState(UiState.LOADING)
        if (validate(login, password, repeatPassword))
            launchHeavy {
                when (val result = signUpUseCase(CredentialsParam(login, password))) {
                    SignUpUseCase.Result.Success -> emitEvent(DataDrivenEvent.NavigateLoggedIn)
                    SignUpUseCase.Result.LoginTaken -> emitEvent(DataDrivenEvent.NotifyLoginTaken)
                    is SignUpUseCase.Result.Error -> {
                        Log.w(TAG, result.cause)
                        emitEvent(DataDrivenEvent.NotifyError(null))
                    }
                }
                updateState(UiState.REGULAR)
            }
    }

    private fun validate(login: String, password: String, repeatPassword: String? = null): Boolean {
        when {
            login.length < 3 || login.length > 20 -> updateState(UiState.LOGIN_ILLEGAL_LENGTH)
            login.contains(Regex("\\W")) -> updateState(UiState.LOGIN_ILLEGAL_CHAR)
            password.length < 8 || password.length > 20 -> updateState(UiState.PASSWORD_ILLEGAL_LENGTH)
            repeatPassword != null && password != repeatPassword -> updateState(UiState.PASSWORDS_DIFFER)
            else -> return true
        }
        return false
    }

    private fun updateState(state: UiState) {
        _uiState.value = state
    }

    private fun emitEvent(event: DataDrivenEvent) {
        launch {
            _events.emit(event)
        }
    }

    enum class UiState {
        REGULAR,
        LOADING,
        LOGIN_ILLEGAL_CHAR,
        LOGIN_ILLEGAL_LENGTH,
        PASSWORDS_DIFFER,
        PASSWORD_ILLEGAL_LENGTH
    }

    sealed interface DataDrivenEvent {
        data object NavigateLoggedIn: DataDrivenEvent
        data object NavigateSignIn: DataDrivenEvent
        data object NavigateSignUp: DataDrivenEvent
        data object NotifyLoginTaken: DataDrivenEvent
        data object NotifyWrongCredentials: DataDrivenEvent
        data class NotifyError(val message: String?): DataDrivenEvent
    }
}