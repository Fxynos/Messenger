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

    fun generateToken(id: Int, login: String, password: ByteArray, expiration: Int): String {
        return Jwts.builder()
            .subject("$id")
            .expiration(Date(System.currentTimeMillis() + expiration))
            .claim("login", login)
            .claim("password", Base64.getEncoder().encodeToString(password))
            .encryptWith(secret, Jwts.ENC.A256GCM)
            .compact()
    }

    fun authenticate(token: String): Authentication? =
        if (validateToken(token))
            getClaims(token).run {
                JwtAuth(
                    subject.toInt(),
                    get("login", String::class.java),
                    Base64.getDecoder().decode(get("password", String::class.java)),
                    true
                )
            }
        else null

    private fun validateToken(token: String): Boolean {
        val claims: Claims = try {
            Jwts.parser().decryptWith(secret).build().parseEncryptedClaims(token).payload
        } catch (exception: Exception) {
            logger.info("invalid token: ${exception.message}")
            return false
        }
        return dataMapper.getVerboseUser(claims.subject.toInt())?.run {
            Base64.getEncoder().encodeToString(password) == claims.get("password", String::class.java) &&
                    login == claims.get("login", String::class.java)
        } ?: false
    }

    private fun getClaims(token: String): Claims =
        Jwts.parser().decryptWith(secret).build().parseEncryptedClaims(token).payload!!

    private class JwtAuth(
        val id: Int,
        val login: String,
        val password: ByteArray,
        private var isAuthenticated: Boolean
    ): Authentication {
        override fun getName() = login
        override fun getAuthorities() = listOf<GrantedAuthority>()
        override fun getCredentials() = password
        override fun getDetails() = null
        override fun getPrincipal() = id
        override fun isAuthenticated() = isAuthenticated
        override fun setAuthenticated(isAuthenticated: Boolean) {
            this.isAuthenticated = isAuthenticated
        }
    }
}