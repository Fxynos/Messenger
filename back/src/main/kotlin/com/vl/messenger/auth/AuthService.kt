package com.vl.messenger.auth

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
        const val TOKEN_TTL_SEC = 24 * 60 * 60 // day
    }

    fun registerUser(login: String, password: String) {
        dataMapper.addUser(login, passwordObfuscator.hashWithSalt(password))
    }

    fun authorize(login: String, password: String): Token? =
        dataMapper.getVerboseUser(login)
            ?.takeIf { passwordObfuscator.approve(password, it.password) }
            ?.let {
                Token(
                    it.id,
                    jwtService.generateToken(it.id, it.login, it.password, TOKEN_TTL_SEC),
                    TOKEN_TTL_SEC
                )
            }

    fun exists(login: String): Boolean {
        return dataMapper.getVerboseUser(login) != null
    }

    data class Token(val userId: Int, val token: String, val expirationSec: Int)
}