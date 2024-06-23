package com.vl.messenger.data.component

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.RemoteMediator
import com.vl.messenger.data.entity.Message
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class PrivateMessagesRepository(
    private val remoteMediator: RemoteMediator<Long, Message>,
    private val pagingSourceFactory: () -> PagingSource<Long, Message>
) {
    fun getMessages(): Flow<PagingData<Message>> = Pager(
        config = PagingConfig(pageSize = 20),
        remoteMediator = remoteMediator,
        pagingSourceFactory = pagingSourceFactory
    ).flow
}