package com.vl.messenger.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vl.messenger.R
import java.util.stream.Stream

class AuthViewModel(app: Application): AndroidViewModel(app) {

    val route = MutableLiveData(Route.SIGN_IN)
    val loginError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val repeatPasswordError = MutableLiveData<String?>()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    enum class Route {
        SIGN_IN,
        SIGN_UP,
        CLOSE
    }

    fun navigateToSignIn() {
        resetErrors()
        route.value = Route.SIGN_IN
    }

    fun navigateToSignUp() {
        resetErrors()
        route.value = Route.SIGN_UP
    }

    fun signIn(login: String, password: String) {
        if (validate(login, password)) {
            // TODO actually sign in
            route.value = Route.CLOSE
        }
    }

    fun signUp(login: String, password: String, repeatPassword: String) {
        if (validate(login, password, repeatPassword)) {
            // TODO actually sign up
            route.value = Route.CLOSE
        }
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

    private fun resetErrors() = Stream.of(loginError, passwordError, repeatPasswordError)
        .forEach { it.value = null }
}