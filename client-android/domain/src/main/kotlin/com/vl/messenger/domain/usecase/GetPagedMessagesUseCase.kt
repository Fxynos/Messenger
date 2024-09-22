package com.vl.messenger.domain.usecase

import androidx.paging.PagedList
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.entity.Message

class GetPagedMessagesUseCase(
    private val dataSource: MessageDataSource
): BlockingUseCase<Unit, PagedList<Message>> {
    override fun invoke(param: Unit): PagedList<Message> = dataSource.getMessages()
}