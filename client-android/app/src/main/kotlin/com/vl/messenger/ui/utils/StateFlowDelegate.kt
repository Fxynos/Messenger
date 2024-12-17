package com.vl.messenger.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> ViewModel.fetch(initial: T, block: suspend () -> T): ReadOnlyProperty<Any?, T> =
    block.asFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initial)
        .let(::StateFlowDelegate)

private class StateFlowDelegate<T>(private val state: StateFlow<T>): ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value
}