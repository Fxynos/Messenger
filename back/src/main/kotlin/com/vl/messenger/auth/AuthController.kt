package com.vl.messenger.auth

import com.vl.messenger.auth.dto.AuthResponse
import com.vl.messenger.auth.dto.RegistrationForm
import com.vl.messenger.dto.StatusResponse
import com.vl.messenger.statusOf
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(@Autowired private val authService: AuthService) {

    @PostMapping("/sign-up")
    fun register(@Valid @RequestBody form: RegistrationForm): ResponseEntity<StatusResponse<Any>> {
        if (authService.exists(form.login))
            return statusOf(HttpStatus.CONFLICT, "Login is taken")
        authService.registerUser(form.login, form.password)
        return statusOf(HttpStatus.OK, "Account is created")
    }

    @PostMapping("/sign-in")
    fun authorize(@Valid @RequestBody form: RegistrationForm): ResponseEntity<StatusResponse<AuthResponse>> {
        fun unauthorizedStatus() = statusOf<AuthResponse>(HttpStatus.UNAUTHORIZED, "Wrong login or password")
        if (!authService.exists(form.login))
            return unauthorizedStatus()
        return statusOf(
            payload = authService.authorize(form.login, form.password)
                ?.run { AuthResponse(userId, token, expirationSec) }
                ?: return unauthorizedStatus()
        )
    }
}