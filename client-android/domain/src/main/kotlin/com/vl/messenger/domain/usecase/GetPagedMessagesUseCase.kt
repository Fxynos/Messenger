package com.vl.messenger.domain.usecase

import androidx.paging.PagedList
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.usecase.param.DialogParam
import kotlinx.coroutines.runBlocking

class GetPagedMessagesUseCase(
    private val dataSource: MessageDataSource,
    private val sessionStore: SessionStore
): BlockingUseCase<DialogParam, PagedList<Message>> {

    private val token: String get() = runBlocking { sessionStore.getToken()!!.token }

    override fun invoke(param: DialogParam): PagedList<Message> = when (param) {
        is DialogParam.PrivateDialog -> dataSource.getMessages(token, param.userId)
        is DialogParam.Conversation -> dataSource.getMessages(token, param.conversationId)
    }
}