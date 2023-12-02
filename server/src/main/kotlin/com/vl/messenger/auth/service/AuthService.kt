package com.vl.messenger.auth.service

import com.vl.messenger.auth.dao.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val jwtService: JwtService,
    @Autowired private val passwordObfuscator: PasswordObfuscator
) {
    companion object {
        const val TOKEN_TTL_MS = 60_000 // minute
    }

    fun registerUser(login: String, password: String) {
        userRepository.addUser(login, passwordObfuscator.hashWithSalt(password))
    }

    fun authorize(login: String, password: String): Token? =
        userRepository.getPasswordHash(login)
            ?.takeIf { passwordObfuscator.approve(password, it) }
            ?.let { hash ->
                Token(
                    jwtService.generateToken(login, hash, TOKEN_TTL_MS),
                    TOKEN_TTL_MS
                )
            }

    fun exists(login: String): Boolean {
        return userRepository.getPasswordHash(login) != null
    }

    data class Token(val token: String, val expirationMs: Int)
}