package com.vl.messenger.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.PagingSource
import androidx.paging.RemoteMediator
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.entity.Message

@OptIn(ExperimentalPagingApi::class)
class PrivateMessageDataSource(
    private val remoteMediator: RemoteMediator<Long, Message>,
    private val pagingSourceFactory: () -> PagingSource<Long, Message>
): MessageDataSource, ItemKeyedDataSource<Long, Message>() {

    override fun getMessages(): PagedList<Message> {
        TODO("Not yet implemented")
    }

    override fun getKey(item: Message): Long = item.id

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Message>) {
        TODO("Not yet implemented")
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Message>) {
        TODO("Not yet implemented")
    }

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Message>
    ) {
        params.
    }
}