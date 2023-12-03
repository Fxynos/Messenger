package com.vl.messenger.auth.service

import com.vl.messenger.DataMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Autowired private val dataMapper: DataMapper,
    @Autowired private val jwtService: JwtService,
    @Autowired private val passwordObfuscator: PasswordObfuscator
) {
    companion object {
        const val TOKEN_TTL_MS = 24 * 60 * 60 * 1000 // day
    }

    fun registerUser(login: String, password: String) {
        dataMapper.addUser(login, passwordObfuscator.hashWithSalt(password))
    }

    fun authorize(login: String, password: String): Token? =
        dataMapper.getPasswordHash(login) // TODO single query for id and hash
            ?.takeIf { passwordObfuscator.approve(password, it) }
            ?.let { hash ->
                Token(
                    jwtService.generateToken(dataMapper.getUserId(login)!!, login, hash, TOKEN_TTL_MS),
                    TOKEN_TTL_MS
                )
            }

    fun exists(login: String): Boolean {
        return dataMapper.getPasswordHash(login) != null
    }

    data class Token(val token: String, val expirationMs: Int)
}