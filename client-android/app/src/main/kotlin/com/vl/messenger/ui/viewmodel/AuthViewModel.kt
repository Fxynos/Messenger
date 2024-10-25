package com.vl.messenger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.usecase.GetIsLoggedInUseCase
import com.vl.messenger.domain.usecase.SignInUseCase
import com.vl.messenger.domain.usecase.SignUpUseCase
import com.vl.messenger.domain.usecase.param.CredentialsParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        viewModelScope.launch {
            _events.emit(
                if (getIsLoggedInUseCase(Unit))
                    DataDrivenEvent.LOGGED_IN
                else
                    DataDrivenEvent.NAVIGATE_SIGN_IN
            )
        }
    }

    fun navigateToSignIn() {
        if (uiState.value != UiState.LOADING)
            emitEvent(DataDrivenEvent.NAVIGATE_SIGN_IN)
    }

    fun navigateToSignUp() {
        if (uiState.value != UiState.LOADING)
            emitEvent(DataDrivenEvent.NAVIGATE_SIGN_UP)
    }

    fun signIn(login: String, password: String) {
        updateState(UiState.LOADING)
        if (validate(login, password))
            viewModelScope.launch {
                when (signInUseCase(CredentialsParam(login, password))) {
                    SignInUseCase.Result.SUCCESS -> emitEvent(DataDrivenEvent.LOGGED_IN)
                    SignInUseCase.Result.WRONG_CREDENTIALS -> emitEvent(DataDrivenEvent.NOTIFY_WRONG_CREDENTIALS)
                    SignInUseCase.Result.ERROR -> emitEvent(DataDrivenEvent.NOTIFY_ERROR)
                }
                updateState(UiState.REGULAR)
            }
    }

    fun signUp(login: String, password: String, repeatPassword: String) {
        updateState(UiState.LOADING)
        if (validate(login, password, repeatPassword))
            viewModelScope.launch {
                when (signUpUseCase(CredentialsParam(login, password))) {
                    SignUpUseCase.Result.SUCCESS -> emitEvent(DataDrivenEvent.LOGGED_IN)
                    SignUpUseCase.Result.LOGIN_TAKEN -> emitEvent(DataDrivenEvent.NOTIFY_LOGIN_TAKEN)
                    SignUpUseCase.Result.ERROR -> emitEvent(DataDrivenEvent.NOTIFY_ERROR)
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
        viewModelScope.launch {
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

    enum class DataDrivenEvent {
        LOGGED_IN,
        NAVIGATE_SIGN_IN,
        NAVIGATE_SIGN_UP,
        NOTIFY_LOGIN_TAKEN,
        NOTIFY_WRONG_CREDENTIALS,
        NOTIFY_ERROR
    }
}