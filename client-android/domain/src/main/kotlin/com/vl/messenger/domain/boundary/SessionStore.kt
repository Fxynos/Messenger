package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.AccessToken
import kotlinx.coroutines.flow.Flow

interface SessionStore {
    suspend fun setToken(token: AccessToken)
    suspend fun removeToken()
    fun observeToken(): Flow<AccessToken?>
}