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

}