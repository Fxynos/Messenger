package com.vl.messenger.data.manager

import com.google.gson.Gson
import com.vl.messenger.BuildConfig
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.StompMessage
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class PrivateChatManager(
    private val profileManager: ProfileManager,
    sessionStore: SessionStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var connection: StompClient? = null
    private var chatDisposable: Disposable? = null
    private var lifecycleDisposable: Disposable? = null
    private val _messageEvents = MutableSharedFlow<Message>()
    val messageEvents = _messageEvents.asSharedFlow()

    init {
        scope.launch {
            connection = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                "ws://${BuildConfig.ADDRESS}/ws",
                mapOf("Authorization" to "Bearer ${sessionStore.accessTokenFlow.value!!.token}")
            ).apply {
                connect()
                lifecycleDisposable = lifecycle().subscribe { event ->
                    if (event.exception != null)
                        throw event.exception

                    when (event.type!!) {
                        LifecycleEvent.Type.CLOSED -> Unit
                        LifecycleEvent.Type.OPENED -> start()
                        LifecycleEvent.Type.ERROR,
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> reconnect()
                    }
                }
            }
        }
    }

    private fun start() {
        chatDisposable?.dispose()
        chatDisposable = connection!!.topic(
            "/users/${profileManager.getProfile().id}/chat"
        ).subscribe { stompMsg ->
            val payload = Gson().fromJson(stompMsg.payload, StompMessage::class.java)
            if (payload.conversationId == null)
                scope.launch {
                    _messageEvents.emit(Message(
                        payload.id!!,
                        payload.senderId!!,
                        System.currentTimeMillis(),
                        payload.content
                    ))
                }
        }
    }

    fun release() {
        scope.cancel()
        lifecycleDisposable?.dispose()
        chatDisposable?.dispose()
        connection?.disconnect()
    }
}