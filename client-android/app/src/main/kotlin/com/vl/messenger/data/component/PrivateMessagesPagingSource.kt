package com.vl.messenger.data.component

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.manager.DialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivateMessagesPagingSource(
    private val dialogManager: DialogManager,
    private val userId: Int
): PagingSource<Long, Message>() {

    override fun getRefreshKey(state: PagingState<Long, Message>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Message> =
        withContext(Dispatchers.IO) {
            dialogManager.getMessages(userId, params.loadSize, params.key).let { messages ->
                LoadResult.Page(
                    messages,
                    null,
                    if (messages.size < params.loadSize) null else messages.last().id
                )
            }
        }
}