package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerStompApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ObserveIncomingMessagesUseCase(
    private val stompApi: MessengerStompApi,
    private val sessionStore: SessionStore
): FlowUseCase<Unit, Message> {
    override fun invoke(param: Unit): Flow<Message> = stompApi.subscribeOnIncomingMessages(
        runBlocking {
            sessionStore.observeToken().first()!!
        }
    )
}