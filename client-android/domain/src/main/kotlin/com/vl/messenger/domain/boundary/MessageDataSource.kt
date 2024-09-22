package com.vl.messenger.domain.boundary

import androidx.paging.PagedList
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    fun getMessages(): PagedList<Message>
}