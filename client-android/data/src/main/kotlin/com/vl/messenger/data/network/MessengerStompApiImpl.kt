package com.vl.messenger.data.network

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.vl.messenger.data.network.NetworkMapper.toDomain
import com.vl.messenger.data.network.dto.StompMessage
import com.vl.messenger.domain.boundary.MessengerStompApi
import com.vl.messenger.domain.entity.AccessToken
import com.vl.messenger.domain.entity.Message
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

private const val TAG = "StompApi"

/**
 * @param address e.g. `localhost:8080`
 */
class MessengerStompApiImpl(private val address: String): MessengerStompApi {
    @SuppressLint("CheckResult")
    override fun subscribeOnIncomingMessages(accessToken: AccessToken): Flow<Message> = channelFlow {
        var lifecycleDisposable: Disposable
        var connectionDisposable: Disposable? = null

        fun subscribe(connection: StompClient): Disposable =
            connection.topic(
                "/users/${accessToken.userId}/chat"
            ).subscribe { stompMessage ->
                runBlocking {
                    send(
                        Gson().fromJson(stompMessage.payload, StompMessage::class.java)
                            .toDomain()
                    )
                }
            }

        Log.d(TAG, "Connecting")

        val connection = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://$address/ws",
            mapOf("Authorization" to accessToken.token.toBearerAuthHeader())
        ).also { connection ->
            connection.connect()
            lifecycleDisposable = connection.lifecycle().subscribe { event ->
                if (event.exception != null)
                    throw event.exception

                when (event.type!!) {
                    LifecycleEvent.Type.CLOSED -> Unit
                    LifecycleEvent.Type.OPENED -> {
                        connectionDisposable?.dispose()
                        connectionDisposable = subscribe(connection)
                    }
                    LifecycleEvent.Type.ERROR,
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> connection.reconnect()
                }
            }
        }
        awaitClose {
            Log.d(TAG, "Disconnecting")
            connection.disconnect()
            lifecycleDisposable.dispose()
            connectionDisposable?.dispose()
        }
    }
}