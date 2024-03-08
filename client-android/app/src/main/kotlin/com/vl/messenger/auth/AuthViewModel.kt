package com.vl.messenger.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vl.messenger.App
import com.vl.messenger.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.stream.Stream

class AuthViewModel(app: App): AndroidViewModel(app) {
    val route = MutableLiveData(Route.SIGN_IN) // TODO use StateFlow
    val popup = MutableLiveData<InfoPopup?>()
    val isButtonEnabled = MutableLiveData(true)
    val loginError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val repeatPasswordError = MutableLiveData<String?>()

    private val context: Context
        get() = getApplication<Application>().applicationContext
    private val authManager = AuthManager(app.retrofit)
    private val sessionStore = SessionStore(app)
    private var currentTask: Job? = null // sign in or sign up job

    init {
        if (sessionStore.accessTokenFlow.value != null)
            route.value = Route.CLOSE
    }

    fun navigateToSignIn() {
        currentTask?.takeUnless(Job::isCompleted)?.cancel()
        resetState()
        route.value = Route.SIGN_IN
    }

    fun navigateToSignUp() {
        currentTask?.takeUnless(Job::isCompleted)?.cancel()
        resetState()
        route.value = Route.SIGN_UP
    }

    fun attemptSignIn(login: String, password: String) {
        if (validate(login, password))
            currentTask = viewModelScope.launch { signIn(login, password) }
    }

    fun attemptSignUp(login: String, password: String, repeatPassword: String) {
        if (validate(login, password, repeatPassword))
            currentTask = viewModelScope.launch { signUp(login, password) }
    }

    private fun validate(login: String, password: String, repeatPassword: String? = null): Boolean {
        when {
            login.length < 3 || login.length > 20 ->
                loginError.value = context.getString(R.string.error_login_length)
            login.contains(Regex("\\W")) ->
                loginError.value = context.getString(R.string.error_login_char)
            else ->
                loginError.value = null
        }
        passwordError.value =
            if (password.length < 8 || password.length > 20)
                context.getString(R.string.error_password_length)
            else null
        repeatPasswordError.value =
            if (repeatPassword != null && password != repeatPassword)
                context.getString(R.string.error_password_repeat)
            else null
        return Stream.of(loginError, passwordError, repeatPasswordError)
            .map { it.value }
            .anyMatch { it != null }
            .not() // there are no errors if true
    }

    private suspend fun signIn(login: String, password: String) {
        withContext(Dispatchers.Main) {
            isButtonEnabled.value = false
        }
        val result = withContext(Dispatchers.IO) { authManager.signIn(login, password) }
        withContext(Dispatchers.Main) {
            when (result) {
                is AuthManager.SignInResult.Token -> {
                    saveToken(result)
                    route.value = Route.CLOSE
                }
                is AuthManager.SignInResult.WrongCredentials -> popup.value = InfoPopup(
                    context.getString(R.string.title_could_not_sign_in),
                    context.getString(R.string.info_wrong_credentials)
                )
                null -> popup.value = InfoPopup(
                    context.getString(R.string.title_could_not_sign_in),
                    context.getString(R.string.info_unexpected_error)
                )
            }
            isButtonEnabled.value = true
        }
    }

    private suspend fun signUp(login: String, password: String) {
        withContext(Dispatchers.Main) {
            isButtonEnabled.value = false
        }
        val result = withContext(Dispatchers.IO) { authManager.signUp(login, password) }
        withContext(Dispatchers.Main) {
            when (result) {
                is AuthManager.SignUpResult.Success ->
                    signIn(login, password)
                is AuthManager.SignUpResult.LoginIsTaken -> popup.value = InfoPopup(
                    context.getString(R.string.title_could_not_sign_in),
                    context.getString(R.string.info_login_taken)
                )
                null -> popup.value = InfoPopup(
                    context.getString(R.string.title_could_not_sign_in),
                    context.getString(R.string.info_unexpected_error)
                )
            }
            isButtonEnabled.value = true
        }
    }

    private suspend fun saveToken(token: AuthManager.SignInResult.Token) {
        sessionStore.setAccessToken(SessionStore.AccessToken(token.token, token.expirationSec))
    }

    private fun resetState() {
        isButtonEnabled.value = true
        Stream.of(loginError, passwordError, repeatPasswordError, popup).forEach { it.value = null }
    }

    enum class Route {
        SIGN_IN,
        SIGN_UP,
        CLOSE
    }

    inner class InfoPopup(val title: String, val text: String) {
        fun hide() {
            popup.value = null
        }
    }

    class Factory(private val app: App): ViewModelProvider.AndroidViewModelFactory(app) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (AuthViewModel::class.java.isAssignableFrom(modelClass))
                AuthViewModel(app) as T
            else super.create(modelClass)
        }
    }
}