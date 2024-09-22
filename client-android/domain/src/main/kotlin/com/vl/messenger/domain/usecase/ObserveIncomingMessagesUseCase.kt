package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerStompApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class ObserveIncomingMessagesUseCase(
    private val messengerApi: MessengerStompApi,
    private val sessionStore: SessionStore
): FlowUseCase<Unit, Message> {
    override fun invoke(param: Unit): Flow<Message> = flow {
        messengerApi.subscribeOnIncomingMessages(
            runBlocking { sessionStore.getToken()!! }
        ).collect { emit(it) }
    }
}