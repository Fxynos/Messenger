package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface SessionStore {
    suspend fun getToken(): AccessToken? = observeToken().first()
    suspend fun setToken(token: AccessToken)
    suspend fun removeToken()
    fun observeToken(): Flow<AccessToken?>
}