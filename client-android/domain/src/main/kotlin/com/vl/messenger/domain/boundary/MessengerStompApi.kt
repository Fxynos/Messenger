package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.AccessToken
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessengerStompApi {
    fun subscribeOnIncomingMessages(accessToken: AccessToken): Flow<Message>
}