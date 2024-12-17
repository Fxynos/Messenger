package com.vl.messenger.data.paging.message

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.domain.boundary.PagingCache
import com.vl.messenger.domain.entity.Message

internal class MessagePagingSource(
    private val cache: PagingCache<Long, Message>
): PagingSource<Long, Message>() {

    override fun getRefreshKey(state: PagingState<Long, Message>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Message> {
        val messages = cache.getPage(params.key, params.loadSize)
        return LoadResult.Page(
            data = messages,
            prevKey = null,
            nextKey =
                if (messages.size < params.loadSize) null
                else messages.last().id
        )
    }
}