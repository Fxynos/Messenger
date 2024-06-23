package com.vl.messenger.data.component

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.manager.DialogManager
import com.vl.messenger.domain.Dao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PrivateMessagesPagingSource"

class PrivateMessagesPagingSource(
    private val dao: Dao<Long, Message>
): PagingSource<Long, Message>() {

    override fun getRefreshKey(state: PagingState<Long, Message>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Message> {
        Log.d(TAG, "load ${params.key}")
        val items = dao.getPage(params.key, params.loadSize)
        return LoadResult.Page(items, null, items.lastOrNull()?.id)
    }
}