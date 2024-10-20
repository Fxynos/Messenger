package com.vl.messenger.data.network

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.vl.messenger.data.network.NetworkMapper.toDomain
import com.vl.messenger.data.network.dto.StompMessage
import com.vl.messenger.domain.boundary.MessengerStompApi
import com.vl.messenger.domain.entity.AccessToken
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

/**
 * @param address e.g. `localhost:8080`
 */
class MessengerStompApiImpl(private val address: String): MessengerStompApi {
    @SuppressLint("CheckResult")
    override fun subscribeOnIncomingMessages(accessToken: AccessToken): Flow<Message> = flow {

        fun subscribe(connection: StompClient) {
            connection.topic(
                "/users/${accessToken.userId}/chat"
            ).subscribe { stompMessage ->
                runBlocking {
                    emit(
                        Gson().fromJson(stompMessage.payload, StompMessage::class.java)
                            .toDomain()
                    )
                }
            }
        }

        Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://$address/ws",
            mapOf("Authorization" to accessToken.token.toBearerAuthHeader())
        ).also { connection ->
            connection.connect()
            connection.lifecycle().subscribe { event ->
                if (event.exception != null)
                    throw event.exception

                when (event.type!!) {
                    LifecycleEvent.Type.CLOSED -> Unit
                    LifecycleEvent.Type.OPENED -> subscribe(connection)
                    LifecycleEvent.Type.ERROR,
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> connection.reconnect()
                }
            }
        }

    }
}