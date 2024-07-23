package com.vl.messenger.data.component

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.data.entity.Message
import com.vl.messenger.domain.Dao

class PrivateMessagePagingSource(
    private val dao: Dao<Long, Message>
): PagingSource<Long, Message>() {

    override fun getRefreshKey(state: PagingState<Long, Message>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Message> {
        val items = dao.getPage(params.key, params.loadSize)
        return LoadResult.Page(items, null, items.lastOrNull()?.id)
    }
}