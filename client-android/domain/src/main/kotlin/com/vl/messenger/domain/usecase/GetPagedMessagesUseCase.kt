package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.CachedPagingData
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.runBlocking

class GetPagedMessagesUseCase (
    private val sessionStore: SessionStore,
    private val messageDataSource: MessageDataSource
): SuspendedUseCase<String, CachedPagingData<Long, Message>> {
    /**
     * @param param dialog id
     */
    override suspend fun invoke(param: String): CachedPagingData<Long, Message> =
        messageDataSource.getMessages(runBlocking {sessionStore.getToken()!!.token }, param)
}