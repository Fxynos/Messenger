package com.vl.messenger.data.paging.message

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.vl.messenger.domain.boundary.PagingCache
import com.vl.messenger.domain.entity.Message

@OptIn(ExperimentalPagingApi::class)
internal class MessageRemoteMediator(
    private val cache: PagingCache<Long, Message>,
    private val fetch: suspend (key: Long?, limit: Int) -> List<Message>
): RemoteMediator<Long, Message>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Long, Message>
    ): MediatorResult {
        val pageSize by state.config::pageSize
        return when (loadType) {
            LoadType.PREPEND -> MediatorResult.Success(true)

            LoadType.APPEND -> {
                val items = fetch(state.lastItemOrNull()?.id, pageSize)
                cache.addLast(items)
                MediatorResult.Success(items.size < pageSize)
            }

            LoadType.REFRESH -> {
                val items = fetch(null, pageSize)
                cache.addLast(items)
                MediatorResult.Success(items.size < pageSize)
            }
        }
    }
}