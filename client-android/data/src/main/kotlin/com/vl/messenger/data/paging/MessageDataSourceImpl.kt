package com.vl.messenger.data.paging

import androidx.paging.PagedList
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.runBlocking

private const val PAGE_SIZE = 10

class MessageDataSourceImpl(private val messengerApi: MessengerRestApi): MessageDataSource {
    override fun getMessages(token: String, userId: Int): PagedList<Message> =
        PagedList.Builder(
            MessageDataSourceAdapter { limit, from ->
                runBlocking { messengerApi.getMessages(
                    token = token,
                    userId = userId,
                    limit, from
                ) }
            }, PAGE_SIZE
        ).build()

    override fun getMessages(token: String, conversationId: Long): PagedList<Message> =
        PagedList.Builder(
            MessageDataSourceAdapter { limit, from ->
                runBlocking { messengerApi.getMessages(
                    token = token,
                    conversationId = conversationId,
                    limit, from
                ) }
            }, PAGE_SIZE
        ).build()
}