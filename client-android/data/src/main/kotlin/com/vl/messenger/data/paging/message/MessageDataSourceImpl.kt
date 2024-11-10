package com.vl.messenger.data.paging.message

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.entity.CachedPagingData
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalPagingApi::class)
class MessageDataSourceImpl(
    private val api: MessengerRestApi
): MessageDataSource {
    companion object {
        private const val PAGE_SIZE = 10
    }

    override suspend fun getMessages(token: String, dialogId: String): CachedPagingData<Long, Message> {
        val cache = MessagePagingCache()
        val request: suspend (key: Long?, limit: Int) -> List<Message> = { key, limit ->
            api.getMessages(token, dialogId, limit, key)
        }
        val scope = CoroutineScope(coroutineContext)
        val data = Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = {
                val source = MessagePagingSource(cache)
                scope.launch {
                    cache.updateEvents.collectLatest {
                        Log.d("MessageDataSourceImpl", "invalidate paging source")
                        source.invalidate()
                    }
                }
                source
            },
            remoteMediator = MessageRemoteMediator(cache, request)
        ).flow
        return CachedPagingData(cache, data)
    }
}