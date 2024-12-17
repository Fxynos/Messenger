package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class SessionStore {
    fun requireToken(): AccessToken = runBlocking { getToken()!! }
    suspend fun getToken(): AccessToken? = observeToken().first()
    abstract suspend fun setToken(token: AccessToken)
    abstract suspend fun removeToken()
    abstract fun observeToken(): Flow<AccessToken?>
}