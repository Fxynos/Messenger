package com.vl.messenger.domain.boundary

interface PagingCache<K, V> {
    val first: V?
    val last: V?

    operator fun get(key: K): V
    operator fun set(key: K, value: V)
    fun getPage(key: K?, size: Int): List<V>
    fun addLast(items: List<V>)
    fun addFirst(items: List<V>)
}