package com.vl.messenger.domain.entity

/**
 * @param expiration unix time in seconds
 */
data class AccessToken(
    val token: String,
    val expiration: Long,
    val userId: Int
) {
    companion object {
        const val EXPIRATION_LIMITLESS = 0
    }
}