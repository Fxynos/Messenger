package com.vl.messenger.data.paging.shared

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class OffsetKeyedPagingSource<V: Any>(
    private val fetch: suspend (offset: Int, limit: Int) -> List<V>
): PagingSource<Int, V>() {

    override fun getRefreshKey(state: PagingState<Int, V>): Int = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, V> {
        val items = withContext(Dispatchers.IO) {
            fetch(params.key ?: 0, params.loadSize)
        }
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey =
                if (items.size < params.loadSize) null
                else items.size + (params.key ?: 0)
        )
    }
}