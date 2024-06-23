package com.vl.messenger.data.component

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.manager.DialogManager
import com.vl.messenger.domain.Dao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PrivateMessagesRemoteMediator"

@OptIn(ExperimentalPagingApi::class)
class PrivateMessagesRemoteMediator(
    private val dialogManager: DialogManager,
    private val dao: Dao<Long, Message>,
    private val companionUserId: Int
): RemoteMediator<Long, Message>() {

    override suspend fun initialize() = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Long, Message>
    ): MediatorResult {
        Log.d(TAG, "load $loadType")

        suspend fun fetch(key: Long?) = withContext(Dispatchers.IO) {
            dialogManager.getMessages(
                companionUserId,
                state.config.pageSize,
                key
            )
        }.map { it.id to it }

        return when (loadType) {
            LoadType.APPEND -> {
                val items = state.lastItemOrNull()?.id?.let { fetch(it) }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                dao.addLast(items)
                MediatorResult.Success(items.size < state.config.pageSize)
            }

            LoadType.PREPEND -> MediatorResult.Success(true)

            LoadType.REFRESH -> {
                val items = fetch(null)
                val endOfPaginationReached = items.size < state.config.pageSize
                Log.d(TAG, "insert ${items.size} items, endReached=$endOfPaginationReached")

                dao.addLast(items)
                MediatorResult.Success(endOfPaginationReached)
            }
        }
    }
}