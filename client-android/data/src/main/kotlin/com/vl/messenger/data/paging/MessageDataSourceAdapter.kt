package com.vl.messenger.data.paging

import androidx.paging.ItemKeyedDataSource
import com.vl.messenger.domain.entity.Message

class MessageDataSourceAdapter(
    private val load: (limit: Int, from: Long?) -> List<Message>
): ItemKeyedDataSource<Long, Message>() {
    override fun getKey(item: Message): Long = item.id

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Message>) =
        callback.onResult(
            load(params.requestedLoadSize, params.key)
        )

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Message>) =
        callback.onResult(emptyList())

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Message>
    ) = callback.onResult(
        load(params.requestedLoadSize, params.requestedInitialKey)
    )
}