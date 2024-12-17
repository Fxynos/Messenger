package com.vl.messenger.domain.boundary

import kotlinx.coroutines.flow.SharedFlow

interface PagingCache<K, V> {
    val first: V?
    val last: V?
    val updateEvents: SharedFlow<Any>

    operator fun get(key: K): V
    operator fun set(key: K, value: V)
    fun getPage(key: K?, size: Int): List<V>
    fun addLast(items: List<V>)
    fun addFirst(items: List<V>)
}