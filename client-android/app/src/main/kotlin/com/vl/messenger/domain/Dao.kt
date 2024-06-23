package com.vl.messenger.domain

interface Dao<K, V> {
    val first: V?
    val last: V?

    operator fun get(key: K): V
    operator fun set(key: K, value: V)
    fun getPage(key: K?, size: Int): List<V>
    fun insert(items: List<Pair<K, V>>)
}