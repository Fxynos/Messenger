package com.vl.messenger.ui.utils

import kotlinx.coroutines.flow.MutableSharedFlow

suspend infix fun <T> T.sendTo(events: MutableSharedFlow<T>) = events.emit(this)