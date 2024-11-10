package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.CachedPagingData
import com.vl.messenger.domain.entity.Message

interface MessageDataSource {
    suspend fun getMessages(token: String, dialogId: String): CachedPagingData<Long, Message>
}