package com.vl.messenger.data.paging.message

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.entity.CachedPagingData
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class MessageDataSourceImpl(
    private val api: MessengerRestApi
): MessageDataSource {
    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun getMessages(token: String, dialogId: String): CachedPagingData<Long, Message> {
        val cache = MessagePagingCache()
        val request: suspend (key: Long?, limit: Int) -> List<Message> = { key, limit ->
            api.getMessages(token, dialogId, limit, key)
        }
        val data = Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { MessagePagingSource(cache) },
            remoteMediator = MessageRemoteMediator(cache, request)
        ).flow
        return CachedPagingData(cache, data)
    }
}