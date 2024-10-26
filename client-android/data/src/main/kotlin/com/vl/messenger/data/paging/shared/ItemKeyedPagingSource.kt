package com.vl.messenger.data.paging.shared

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ItemKeyedPagingSource<K: Any, V: Any>(
    private val fetch: suspend (key: K?, limit: Int) -> List<V>,
    private val idSupplier: V.() -> K
): PagingSource<K, V>() {

    override fun getRefreshKey(state: PagingState<K, V>): K? = null

    override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
        val items = withContext(Dispatchers.IO) {
            fetch(params.key, params.loadSize)
        }
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey =
                if (items.size < params.loadSize) null
                else items.last().idSupplier()
        )
    }
}