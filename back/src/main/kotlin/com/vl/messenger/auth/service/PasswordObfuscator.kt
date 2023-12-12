package com.vl.messenger.auth.service

import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Serves salt in begin of byte arrays, hash does in the end
 */
@Service
class PasswordObfuscator {
    companion object {
        private const val HASH_SALT_SIZE = 16
        private const val HASH_ITERATIONS = 65536
        private const val HASH_SIZE = 128
    }

    private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    private val random = SecureRandom()

    private fun hash(password: String, salt: ByteArray): ByteArray =
        factory.generateSecret(PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, HASH_SIZE)).encoded

    fun hashWithSalt(
        password: String,
        salt: ByteArray = ByteArray(HASH_SALT_SIZE).also { random.nextBytes(it) }
    ): ByteArray {
        val hash = hash(password, salt)
        return ByteBuffer.allocate(HASH_SALT_SIZE + hash.size)
            .put(salt)
            .put(hash)
            .array()
    }

    fun approve(password: String, hashWithSalt: ByteArray): Boolean {
        val salt = ByteArray(HASH_SALT_SIZE)
        val hash = ByteArray(hashWithSalt.size - salt.size)
        ByteBuffer.wrap(hashWithSalt)
            .get(salt)
            .get(hash)
        return hash(password, salt).contentEquals(hash)
    }
}