package com.vl.messenger.auth.service

import com.vl.messenger.DataMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.util.*
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(
    @Value("\${jwt.aes.key}") key: String,
    @Autowired private val dataMapper: DataMapper
) {
    companion object {
        private val logger = Logger.getLogger("JwtService")
    }

    private val secret = SecretKeySpec(Base64.getDecoder().decode(key), "AES")

    fun generateToken(login: String, password: ByteArray, expiration: Int): String {
        return Jwts.builder()
            .subject(login)
            .expiration(Date(System.currentTimeMillis() + expiration))
            .claim("password", Base64.getEncoder().encodeToString(password))
            .encryptWith(secret, Jwts.ENC.A256GCM)
            .compact()
    }

    fun authenticate(token: String): Authentication? =
        if (validateToken(token))
            getClaims(token).run {
                JwtAuth(
                    subject,
                    Base64.getDecoder().decode(get("password", String::class.java)),
                    true
                )
            }
        else null

    private fun validateToken(token: String): Boolean {
        val claims: Claims
        try {
            claims = Jwts.parser().decryptWith(secret).build().parseEncryptedClaims(token).payload
        } catch (exception: Exception) {
            logger.info("invalid token: ${exception.message}")
            return false
        }
        return Base64.getEncoder().encodeToString(
            dataMapper.getPasswordHash(claims.subject)
        ) == claims.get("password", String::class.java)
    }

    private fun getClaims(token: String): Claims =
        Jwts.parser().decryptWith(secret).build().parseEncryptedClaims(token).payload!!

    private class JwtAuth(
        val login: String,
        val password: ByteArray,
        private var isAuthenticated: Boolean
    ): Authentication {
        override fun getName() = login
        override fun getAuthorities() = listOf<GrantedAuthority>()
        override fun getCredentials() = password
        override fun getDetails() = null
        override fun getPrincipal() = login
        override fun isAuthenticated() = isAuthenticated
        override fun setAuthenticated(isAuthenticated: Boolean) {
            this.isAuthenticated = isAuthenticated
        }
    }
}