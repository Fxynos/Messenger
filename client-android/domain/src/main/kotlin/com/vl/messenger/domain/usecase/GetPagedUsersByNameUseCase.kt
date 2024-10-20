package com.vl.messenger.domain.usecase

import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.boundary.UserDataSource
import com.vl.messenger.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class GetPagedUsersByNameUseCase(
    private val sessionStore: SessionStore,
    private val userDataSource: UserDataSource
): FlowUseCase<String, PagingData<User>> {
    /**
     * @param param search pattern
     */
    override fun invoke(param: String): Flow<PagingData<User>> =
        userDataSource.searchByName(runBlocking { sessionStore.getToken()!!.token }, param)
}