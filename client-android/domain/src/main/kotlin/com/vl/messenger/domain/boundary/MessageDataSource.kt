package com.vl.messenger.domain.boundary

import androidx.paging.PagedList
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    fun getMessages(token: String, userId: Int): PagedList<Message>
    fun getMessages(token: String, conversationId: Long): PagedList<Message>
}