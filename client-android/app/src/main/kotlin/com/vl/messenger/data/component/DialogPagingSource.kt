package com.vl.messenger.data.component

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.data.manager.DialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialogPagingSource(private val manager: DialogManager): PagingSource<Int, Dialog>() {

    override fun getRefreshKey(state: PagingState<Int, Dialog>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Dialog> =
        withContext(Dispatchers.IO) {
            val items = manager.getDialogs(params.loadSize, params.key)
            LoadResult.Page(
                items,
                params.key?.takeIf { it > 0 }?.let { it - items.size },
                if (items.size < params.loadSize) null else ((params.key ?: 0) + params.loadSize)
            )
        }
}